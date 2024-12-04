
//Initialize script
import qupath.imagej.gui.ImageJMacroRunner
import qupath.lib.images.servers.PixelType
import qupath.imagej.tools.IJTools
import qupath.lib.regions.RegionRequest
import qupath.lib.objects.PathAnnotationObject
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.io.IOException


def entry = getProjectEntry()
def imageName = entry.getImageName()
def fileName = imageName + ".geojson"
println("Image name: " + imageName)
println("fileName name: " + fileName)

println("project entry: " + entry)


resetDetectionClassifications();
clearSelectedObjects(true);
clearSelectedObjects();
clearSelectedObjects(false);
clearSelectedObjects();
resetDetectionClassifications();

// Get the current project
def project = getProject()
if (project == null) {
    println "No project is currently open."
    return
}

// Get the full path to the project file, including the filename
def projectPath = project.getPath()

// Ensure projectPath is a Path object
println "projectPath is of type: " + projectPath.getClass()

// Get the parent directory path (without the filename)
def projectDir = projectPath.getParent()

// Create the full path to the new file
def newFilePath = projectDir.resolve(fileName)
def save_name = newFilePath.toString()

print "saving annotations" + save_name

exportAllObjectsToGeoJson(save_name, "PRETTY_JSON", "FEATURE_COLLECTION")
resetSelection();
createFullImageAnnotation(true)
clearSelectedObjects(true);
clearSelectedObjects();

print "done"

//runObjectClassifier("original_147thresh");
