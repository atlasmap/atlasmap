const jsonFilePath = "./src/atlasmap.json";
const packageJsonPath = "./package.json";
const fs = require("fs");

const version = require(packageJsonPath).version;
const json = require(jsonFilePath);

json.version = version;

fs.writeFileSync(jsonFilePath, JSON.stringify(json));