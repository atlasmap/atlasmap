import { app, BrowserWindow, screen } from 'electron';
import * as path from 'path';

const { spawn } = require('child_process');
const fs = require('fs');
const os = require('os');
const net = require('net');


function findOpenPort() {
  const server = net.createServer((c) => {
    c.close(c);
  });
  server.listen(0, () => {
    console.log('server bound');
  });
  var port = server.address().port
  server.close()
  return port;
}
const atlasMapServicePort = findOpenPort()


let win, serve;
const args = process.argv.slice(1);
serve = args.some(val => val === '--serve');

if (serve) {
  require('electron-reload')(__dirname, {
  });
}

function createWindow() {

  const electronScreen = screen;
  const size = electronScreen.getPrimaryDisplay().workAreaSize;


  // Create the browser window.
  win = new BrowserWindow({
    x: 0,
    y: 0,
    width: size.width,
    height: size.height,
    webPreferences: {
    'nodeIntegration': true
    }
  });

  setTimeout(()=>{
    // and load the index.html of the app.
    win.loadURL('file://' + __dirname + '/index.html#'+encodeURIComponent(JSON.stringify({atlasMapServicePort:atlasMapServicePort})));
  }, 1000*10 );

  // Open the DevTools.
  if (serve) {
     // win.webContents.openDevTools();
  }

  // Emitted when the window is closed.
  win.on('closed', () => {
    // Dereference the window object, usually you would store window
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    win = null;
  });
}

try {
  
  var jarFile = __dirname+'/assets/runtime-1.32.2.jar';
  console.log("jarFile: ", jarFile);
  if( __dirname.endsWith("/app.asar") ) {    
    var data = fs.readFileSync(jarFile);
    jarFile = os.homedir()+"/.atlasmap-runtime-1.32.2.jar"
    console.log("unpacking to jarFile: ", jarFile);
    fs.writeFileSync(jarFile, data);
  }

  console.log("Booting java runtime on port: ", atlasMapServicePort);
  var javaProcess = spawn('java', ['-jar', jarFile, "--server.port="+atlasMapServicePort], { shell: true, stdio:'inherit' });

  app.on('quit', ()=>{
      console.log("Stopping java runtime...");
      javaProcess.kill();
      javaProcess = null;
  });

  // This method will be called when Electron has finished
  // initialization and is ready to create browser windows.
  // Some APIs can only be used after this event occurs.
  app.on('ready', createWindow);

  // Quit when all windows are closed.
  app.on('window-all-closed', () => {
    // On OS X it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
      app.quit();
    }
  });

  app.on('activate', () => {
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (win === null) {
      createWindow();
    }
  });

} catch (e) {
  // Catch Error
  // throw e;
}
