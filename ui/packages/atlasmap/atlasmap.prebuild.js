/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
const jsonFilePath = "./src/atlasmap.json";
const packageJsonPath = "./package.json";
const srcPath = "./src";
const cssTypeFileSuffix = ".css.d.ts";
const cssFileSuffix = ".css";
const cssModuleFileSuffix = ".module.css";
const fs = require("fs");
const path = require("path");

function verifyModuleCssSuffix(dir, error) {
    if (!fs.existsSync(dir)) {
        return;
    }
    const files = fs.readdirSync(dir);
    for (let file of files) {
        const filename = path.join(dir, file);
        const stat = fs.lstatSync(filename);
        if (stat.isDirectory()) {
            error = verifyModuleCssSuffix(filename, error);
        } else if (filename.endsWith(cssFileSuffix)
            && !filename.endsWith(cssModuleFileSuffix)) {
            console.error("ERROR: " + filename + " has to end with '.module.css'");
            error++;
        }
    }
    return error;
}

function removeCssTypeFiles(dir) {
    if (!fs.existsSync(dir)) {
        return;
    }
    const files = fs.readdirSync(dir);
    for (let file of files) {
        const filename = path.join(dir, file);
        const stat = fs.lstatSync(filename);
        if (stat.isDirectory()) {
            removeCssTypeFiles(filename);
        } else if (filename.endsWith(cssTypeFileSuffix)) {
            fs.unlinkSync(filename);
            console.log("Removed: " + filename);
        }

    }
}

// Create AtlasMap property json file
const version = require(packageJsonPath).version;
const json = { "version": version };
console.log("Version: " + version);
fs.writeFileSync(jsonFilePath, JSON.stringify(json));

// Regenerate *.module.css.d.ts
removeCssTypeFiles(srcPath);
const err = verifyModuleCssSuffix(srcPath, 0);
if (err !== 0) {
    throw Error("Found " + err + " error(s)");
}
const tcm = require("typed-css-modules");
tcm.run(srcPath);