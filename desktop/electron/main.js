const { app, BrowserWindow } = require('electron')
const path = require('path')

const DEV_URL = 'http://localhost:5173'

function createWindow () {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    title: 'Commonplace Book',
    autoHideMenuBar: true,
    webPreferences: {
      contextIsolation: true
    }
  })

  if (app.isPackaged) {
    win.loadFile(path.join(__dirname, '..', 'dist', 'index.html'))
  } else {
    win.loadURL(DEV_URL)
  }
}

app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
