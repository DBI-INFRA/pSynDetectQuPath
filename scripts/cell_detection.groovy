clearDetections();

// Get all annotation objects in the current image
def annotations = getAnnotationObjects()

// Check if there are any annotations
if (annotations.isEmpty()) {
    println "No annotations found in the image."
    return
}

// Select the first annotation
def firstAnnotation = annotations[0]
println "First annotation selected."

// Optional: Perform actions on the first annotation
// For example, set it as the selected object in the viewer
selectObjects(firstAnnotation)
println "First annotation is now selected in the viewer."

// Additional actions can be performed here
// For example, print the ROI of the first annotation
def roi = firstAnnotation.getROI()
println "ROI of the first annotation: " + roi.toString()


runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"Channel 1","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":8.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":2.0,"minAreaMicrons":10.0,"maxAreaMicrons":200.0,"threshold":2.0,"watershedPostProcess":true,"cellExpansionMicrons":5.0,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')
