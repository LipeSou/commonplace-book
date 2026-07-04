import { createApp } from 'vue'
import App from './App.vue'
import './styles/tokens.css'

// as três vozes tipográficas (bundladas, sem CDN)
import '@fontsource/shippori-mincho/600.css'
import '@fontsource/zen-kaku-gothic-new/300.css'
import '@fontsource/zen-kaku-gothic-new/500.css'
import '@fontsource/jetbrains-mono/400.css'

createApp(App).mount('#app')
