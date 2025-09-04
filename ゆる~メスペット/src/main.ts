import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import { useTheme } from '@/composables/useTheme'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
const app = createApp(App)

const { init, listenSystem } = useTheme()
init()
listenSystem()

app.use(createPinia())
app.component('ThemeSwitcher', ThemeSwitcher)
app.use(router)

app.mount('#app')
