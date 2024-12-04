

## Pipeline

Run the follwing script (to run on project, select)

-   export_annotation_script
    - Parameters: No parameters to modify
    - Output: Annotations (region and manual pSyn) will be saved to with suffix (.geojson) 

-   aspect_ratio_w_circularity
    - Parameters: 
    - Output: original image with a fourth channel based on the second channel where too elonged and too small objects will be removed, while annoations will be lost

Make sure to load the new images to the project

-   import_annoation_script
    - Parameters: No parameters to modify
    - Output: image with a fourth channel from previous step and with annoations loaded

-   cell_detection 
    - Parameters: No parameters to modify
    - Output: image with a fourth channel from previous step and with annoations loaded
    - Note: need to make sure that the region annoation is before the pSyn annotation
- run_object_classifier
    - Parameters: `classifier name`: the classifier created
    - Output: image with a fourth channel from previous step and with annoations loaded
    - Note: need to make sure that the region annoation is before the pSyn annotation

Classify -> Object classification -> Create single measurement classifier

[!b](batch.png)

