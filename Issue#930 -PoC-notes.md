# Issue#930 PoC, Support Multiple Mappings Accessed Through Routes

Below is a detailed listing of steps required to test the PoC, please note that most of ways the feature is implemented in this branch was made for PoC purposes and may not be the best way we are able to implement.
# Testing Steps

    1- Clone AtlasMap Project, 
    2- Checkout branch issue#930-poc
    2- Build the project as mentioned in project page [Here]:https://github.com/atlasmap/atlasmap
    3- Run AtlasMap application as indicated in the previous link
    4- Go to url : http://localhost:8585
    5- Add source json document
    6- Add target json document
    7- Do some mappings
    8- Go to url: http://localhost:8585/mapping-id/1
    9- Repeat steps 5,6 and 7
    10- Go to url: http://localhost:8585/mapping-id/2
    11- Repeat steps 5,6 and 7
    12- Revisit the three urls previously stated, you should see the same documets with same mappings persisted accordingly.
    13- Go to ~project_directory/target 
    14- You should find three sets of files for each mapping url you visited: adm, gz, and json







