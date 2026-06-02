import { createApp } from 'vue';
import { createPinia } from 'pinia';
import 'element-plus/dist/index.css';
import 'element-plus/theme-chalk/dark/css-vars.css';
import './styles/index.css';

import App from './App.vue';
import { router } from './router';
import { useThemeStore } from './stores/theme';
import { i18n } from './i18n';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(i18n);
useThemeStore().apply();
app.mount('#app');
