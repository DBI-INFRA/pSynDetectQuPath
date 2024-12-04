def entry = getProjectEntry()
def name = entry.getImageName()

name = name.split("w_c4")[0]

def fileName = name + ".geojson"

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
def full_entry = projectDir.resolve(fileName)
def pathInput = full_entry.toString()

println "loading: " + pathInput

importObjectsFromFile(pathInput)

// Finished
println("Done!")