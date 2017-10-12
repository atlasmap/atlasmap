var fs = require('fs');
var ncp = require('ncp').ncp;
var mkdirp = require('mkdirp');

ncp.limit = 16;

function mkdir(path) {
	console.log("Creating directory: " + path)
	mkdirp.sync(path, function (err) {
    	if (err) console.error(err)
    	else console.log('lib dir created.')
	});
}

function copyFile(sourcePath, targetPath) {
	fs.createReadStream(sourcePath).pipe(fs.createWriteStream(targetPath));
}

function copyPath(sourcePath, targetPath) {
	console.log("Copying folder: " + sourcePath + " => " + targetPath);
	mkdir(targetPath);
	ncp(sourcePath, targetPath, function (err) {
		if (err) {
			return console.error("Could not copy folder: " + sourcePath, err);
		}
		console.log('Folder copied: ' + sourcePath);
	});
}
   

var resources = [
  'node_modules/core-js/client/shim.min.js',
  'node_modules/zone.js/dist/zone.min.js',
];

resources.map(function(f) {
  var path = f.split('/');
  var t = 'aot/dist/' + path[path.length-1];
  copyFile(f, t);
});

copyFile("src/index-aot.html", "aot/dist/index.html");
copyPath("src/assets", "aot/dist/assets");

