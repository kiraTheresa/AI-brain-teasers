import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
// 导入并注册Ant Design图标
import * as AntIcons from '@ant-design/icons-vue'

const app = createApp(App)

// 注册所有Ant Design图标
for (const [key, component] of Object.entries(AntIcons)) {
  app.component(key, component)
}

app.use(router)
app.use(Antd)
app.mount('#app')
