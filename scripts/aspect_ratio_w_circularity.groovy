
//Initialize script
import qupath.imagej.gui.ImageJMacroRunner
import qupath.lib.images.servers.PixelType
import qupath.imagej.tools.IJTools
import qupath.lib.regions.RegionRequest
import qupath.lib.objects.PathAnnotationObject


import qupath.imagej.tools.IJTools
import qupath.imagej.gui.ImageJMacroRunner
import ij.IJ
    
/*
 * parameters that should rarely be changed
 */
DownSamp = 1             // Image downsampling factor
force_all_image = true    // pass all images to imageJ


/*
 * parameters that can be tuned
 */
 
threshlod_level = 2.8    // threshold level for the green/2nd/pSyn Channel
red_threshold = 2.5      // threshold level for the red/1st Channel

gauss = 1                // Gauss filtering of the pSyn channel before masking (in number of pixels)
circularity_low = 0.0    // circularity level, 0 means not used
maxAspectRatio = 3.2    // maximum aspect ratio (long axis/short axis)

size_low = 15            // minimum size to remove too small objects


def entry = getProjectEntry()
def fileName = entry.getImageName()
// Get the current project
def project = getProject()
if (project == null) {
    println "No project is currently open."
    return
}

// Get the full path to the project file, including the filename
def projectPath = project.getPath()
def projectDir = projectPath.getParent()
//def full_entry = projectDir.resolve(fileName)
//def outputFile = full_entry.toString()
projectDir = projectDir.toString()

print 'projectDir = ' + projectDir 
projectDir = projectDir.replace("\\", "\\\\")


// Retrieve image data
imageData = getCurrentImageData()

annotations = getAnnotationObjects()

print '# of annotations = ' + annotations.size()


if (annotations.size()==0 || force_all_image)
{   
    fullImageAnnotation = createFullImageAnnotation(true)
    fireHierarchyUpdate()
    annotations = [fullImageAnnotation]
}
else
{
    selectObjects { p -> p.getPathClass() == getPathClass('Region*') && p.isAnnotation() }
}

print annotations.size()


// Functional parameters

params = new ImageJMacroRunner(getQuPath()).getParameterList()
params.getParameters().get('downsampleFactor').setValue(DownSamp)
params.getParameters().get('sendROI').setValue(true)
params.getParameters().get('sendOverlay').setValue(true)
params.getParameters().get('doParallel').setValue(false)
params.getParameters().get('clearObjects').setValue(false)
params.getParameters().get('getROI').setValue(true)
params.getParameters().get('getOverlay').setValue(true)
params.getParameters().getOverlayAs.setValue("Detections")

String annotationName = ""
// Run the ImageJ macro on each Region
for (annotation in annotations) {
    print annotation
    
    print 'working on ' + annotation.getDisplayedName() 

//        if (annotation.getDisplayedName().contains('pSyn')) {
//           continue 
//        }
    print annotation.hasROI()
    annotationName = annotation.getDisplayedName()
    area = annotation.getROI().getArea()
//    print area
//    print annotationName
    
    if (area == 0 ) {
       continue 
    }
    
    def macro = """
        directory = "${projectDir}";
//        print(directory);
        
        run("Options...", "iterations=1 count=1 black do=Nothing");
        run("Select None");
        title = getTitle();
        print("The current image title is: " + title);
                    
        save_path = directory + File.separator + title + "w_c45";
        
        print("save_path =  " + save_path);
        
        run("Split Channels");
    
        selectImage("C2-" + title);
        
        print("The current image title after splitting is: " +  getTitle());
        
        run("Duplicate...", "title=C4.tif");
        run("Gaussian Blur...", "sigma=${gauss}");
        setThreshold(${threshlod_level}, 255);
//            setAutoThreshold("Otsu dark no-reset");
        run("Convert to Mask");
        
        run("Open");
        run("Erode");
                 
        mask_title =  getTitle();
        print("The current image title after masking is: " + mask_title);
        
        run("Duplicate...", "title=mask_orig");
        mask_title_orig =  getTitle();
        print("The current image title after copy is: " + mask_title_orig);
        
        selectImage("C4.tif");
        run("Set Measurements...", "feret's redirect=None decimal=3");

        run("Analyze Particles...", "display add");
        
        print("The current image title is: " + getTitle());
        
        // Create a new image to store filtered particles
        newImage("Filtered Particles", "8-bit black", getWidth(), getHeight(), 1);
        
        print(nResults);
        selectWindow("Results");
        
        setForegroundColor(255, 255, 255);

        // Loop through each detected particle
        for (i = 0; i < nResults; i++) {
            // Get Feret's Diameter and MinFeret for each particle
            feret = getResult("Feret", i);
            minFeret = getResult("MinFeret", i);
//                print(feret);
//                print(minFeret);
            // Calculate Aspect Ratio
            aspectRatio = feret / minFeret;
            
            // Set the aspect ratio range to keep
            minAspectRatio = 0;  // Lower limit of aspect ratio
            maxAspectRatio = ${maxAspectRatio};  // Upper limit of aspect ratio
            
            // If the particle's aspect ratio is within the range, keep it
            if (aspectRatio >= minAspectRatio && aspectRatio <= maxAspectRatio) {
                // Select the current particle
                roiManager("Select", i);
                run("Fill");
                run("Add Selection...");
                run("Copy");
                selectWindow("Filtered Particles");
                run("Paste");
            }
            if (i % 500 == 0) {
               print(i + " done");
            }
        }
        
        // Close the Results table
        close("Results");

        setAutoThreshold("Otsu dark no-reset");
        run("Convert to Mask");
        
        run("Analyze Particles...", "size=${size_low}-Infinity circularity=${circularity_low}-1.00 show=Masks include");
        
        mask_titele =  getTitle();
        print("The current image title is: " + mask_title);
        title_after_masking = getTitle();
        print("The current image title after masking is: " + title_after_masking);
        selectImage(title_after_masking);
        
        selectImage("C1-" + title);
        run("Duplicate...", "title=C1_mask");
        print("title 1 mask is: " +  getTitle());
        
        selectImage("C1_mask");
        print(getTitle());
        setThreshold(${red_threshold}, 255);
//            setAutoThreshold("Otsu dark no-reset");
        run("Convert to Mask");
        run("Dilate");
        
        
        print("title 1 mask is: " +  getTitle());
        imageCalculator("AND create", "C1_mask", title_after_masking);
        title_after_masking = getTitle();
        print("The current image title after masking title 1 is: " +  getTitle());
       run("Concatenate...", "  title=Mosaic2 open image1=[C1-" + title + "] image2=[C2-" + title + "] image3=[C3-" + title + "] image4=[" + title_after_masking + "] image5=[-- None --]");
        run("Properties...", "channels=4 slices=1 frames=1");
        saveAs("Tiff", save_path);
    """
    

    def imageData = QPEx.getCurrentImageData()
    def imp = IJTools.convertToImagePlus(imageData.getServer())
    imp.show()  // Show the image in ImageJ
    
        
    ImageJMacroRunner.runMacro(params, imageData, null, annotation, macro)

}


print 'done'

if (force_all_image) {
   removeObjects(annotations, true) 
}
