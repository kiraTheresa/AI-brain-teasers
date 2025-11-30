<script setup lang="ts">
import { onMounted, reactive, ref, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { getRooms, postChat } from '@/api'
import { message as antdMessage } from 'ant-design-vue'
// 导入图标组件
import { PlayCircleOutlined, CloseCircleOutlined, SendOutlined } from '@ant-design/icons-vue'

type ChatMsg = { role: 'SYSTEM' | 'USER' | 'ASSISTANT'; content: string }

const route = useRoute()
const roomId = ref<number>(Number(route.params.roomId) || Math.floor(Math.random() * 900000) + 100000)
const input = ref('')
const starting = ref(false)
const started = ref(false)
const ended = ref(false)
const rooms = ref<Array<{ roomId: number; chatMessageList: ChatMsg[] }>>([])
const messages = reactive<ChatMsg[]>([])
const messagesContainer = ref<HTMLElement | null>(null)

// 头像URL
const aiAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=AI'
const userAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=User'

const fetchRooms = async () => {
  try {
    rooms.value = await getRooms()
  } catch (e) {
    antdMessage.error('获取房间列表失败')
  }
}

const startGame = async () => {
  if (started.value || starting.value) return
  starting.value = true
  try {
    const reply = await postChat(roomId.value, '开始')
    messages.push({ role: 'USER', content: '开始' })
    messages.push({ role: 'ASSISTANT', content: reply })
    started.value = true
    if (reply.includes('游戏结束')) ended.value = true
    scrollToBottom()
  } catch (e) {
    antdMessage.error('启动失败')
  } finally {
    starting.value = false
    fetchRooms()
  }
}

const send = async () => {
  const content = input.value.trim()
  if (!content) return
  try {
    const reply = await postChat(roomId.value, content)
    messages.push({ role: 'USER', content })
    messages.push({ role: 'ASSISTANT', content: reply })
    input.value = ''
    if (reply.includes('游戏结束')) ended.value = true
    scrollToBottom()
  } catch (e) {
    antdMessage.error('发送失败')
  } finally {
    fetchRooms()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  fetchRooms()
})

watch(
  () => route.params.roomId,
  (val) => {
    if (val) {
      roomId.value = Number(val)
      messages.splice(0, messages.length)
      started.value = false
      ended.value = false
    }
  }
)
</script>

<template>
  <div class="chat-container">
    <!-- 左侧历史对话列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>💡 AI 脑筋急转弯</h2>
      </div>
      <div class="new-chat-section">
        <a-button 
          type="primary" 
          block 
          class="new-chat-btn"
          @click="$router.push({ name: 'chat', params: { roomId: Math.floor(Math.random() * 900000) + 100000 } })"
        >
          <span class="new-chat-icon">✨</span>
          <span class="new-chat-text">新对话</span>
        </a-button>
      </div>
      <div class="sidebar-content">
        <div class="section-title">历史对话</div>
        <div class="rooms-list">
          <a-button 
            v-for="r in rooms" 
            :key="r.roomId" 
            block 
            class="room-btn" 
            :class="{ active: r.roomId === roomId }"
            @click="$router.push({ name: 'chat', params: { roomId: r.roomId } })"
          >
            <span class="room-icon">💬</span>
            <span class="room-text">对话 {{ r.roomId }}</span>
          </a-button>
        </div>
      </div>
    </div>

    <!-- 右侧聊天主界面 -->
    <div class="chat-main">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <h1>房间 {{ roomId }}</h1>
        <div class="header-actions">
          <a-button 
            type="primary" 
            :disabled="started" 
            :loading="starting" 
            class="start-btn" 
            @click="startGame"
          >
            <template #icon><PlayCircleOutlined /></template>
            开始游戏
          </a-button>
          <a-button 
            danger 
            :disabled="ended" 
            class="end-btn"
          >
            <template #icon><CloseCircleOutlined /></template>
            结束游戏
          </a-button>
        </div>
      </div>

      <!-- 聊天消息区域 -->
      <div class="messages-container" ref="messagesContainer">
        <div 
          v-for="(m, i) in messages" 
          :key="i" 
          class="message-wrapper"
          :class="m.role === 'USER' ? 'user-message' : 'ai-message'"
        >
          <div class="message-avatar">
            <a-avatar :src="m.role === 'USER' ? userAvatar : aiAvatar" />
          </div>
          <div class="message-content-wrapper">
            <div class="message-content">
              {{ m.content }}
            </div>
            <div class="message-time">{{ new Date().toLocaleTimeString() }}</div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">🤖</div>
          <div class="empty-text">点击「开始游戏」开启脑筋急转弯之旅吧！</div>
          <div class="empty-subtext">准备好挑战你的思维了吗？</div>
        </div>
      </div>

      <!-- 消息输入区域 -->
      <div class="input-container">
        <a-input 
          v-model:value="input" 
          placeholder="输入你的答案..." 
          @keyup.enter="send"
          class="message-input"
          :disabled="!started || ended"
        >
          <template #addonAfter>
            <a-button 
              type="primary" 
              @click="send"
              :disabled="!input.trim() || !started || ended"
              class="send-btn"
            >
              <template #icon><SendOutlined /></template>
              发送
            </a-button>
          </template>
        </a-input>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主容器 */
.chat-container {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow: hidden;
}

/* 新对话按钮区域 */
.new-chat-section {
  padding: 0 20px 20px;
}

.new-chat-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  padding: 14px 16px !important;
  font-weight: 600 !important;
  color: white !important;
  transition: all 0.3s ease !important;
  height: auto !important;
  text-align: center !important;
}

.new-chat-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4) !important;
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%) !important;
}

.new-chat-icon {
  margin-right: 8px;
  font-size: 16px;
}

.new-chat-text {
  font-size: 14px;
}

/* 左侧边栏 */
.sidebar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.sidebar-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rooms-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.room-btn {
  transition: all 0.3s ease !important;
  border-radius: 12px !important;
  border: 1px solid #e8e8e8 !important;
  background: white !important;
  color: #666 !important;
  height: auto !important;
  padding: 12px 16px !important;
  text-align: left !important;
  font-weight: 500 !important;
}

.room-btn:hover {
  border-color: #667eea !important;
  background: #f0f4ff !important;
  transform: translateX(4px) !important;
}

.room-btn.active {
  border-color: #667eea !important;
  background: #667eea !important;
  color: white !important;
}

.room-icon {
  margin-right: 8px;
  font-size: 16px;
}

.room-text {
  font-size: 14px;
}

/* 右侧聊天主界面 */
.chat-main {
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chat-header h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.start-btn, .end-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  border-radius: 10px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.end-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}

/* 消息容器 */
.messages-container {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 消息样式 */
.message-wrapper {
  display: flex;
  gap: 12px;
  animation: fadeIn 0.3s ease-out;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  display: flex;
  align-items: flex-end;
}

.message-avatar .ant-avatar {
  width: 40px;
  height: 40px;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.message-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-message .message-content-wrapper {
  align-items: flex-end;
}

.message-content {
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.ai-message .message-content {
  background: white;
  border-bottom-left-radius: 4px;
  color: #333;
}

.user-message .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin: 0 8px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: #666;
}

.empty-icon {
  font-size: 80px;
  animation: bounce 2s infinite;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.empty-subtext {
  font-size: 14px;
  color: #999;
}

/* 输入区域 */
.input-container {
  padding: 24px;
  background: white;
  border-top: 1px solid #f0f0f0;
}

.message-input {
  border-radius: 24px !important;
  border: 2px solid #e8e8e8 !important;
  transition: all 0.3s ease !important;
}

.message-input:focus {
  border-color: #667eea !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2) !important;
}

.send-btn {
  border-radius: 18px !important;
  padding: 8px 20px !important;
  font-weight: 500 !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-20px);
  }
  60% {
    transform: translateY(-10px);
  }
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.5);
  border-radius: 4px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.7);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }
  
  .sidebar {
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  }
  
  .sidebar-content {
    max-height: 100px;
  }
  
  .rooms-list {
    flex-direction: row;
    overflow-x: auto;
    padding-bottom: 10px;
  }
  
  .room-btn {
    white-space: nowrap;
    min-width: 150px;
  }
  
  .chat-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .message-content-wrapper {
    max-width: 85%;
  }
}

@media (max-width: 480px) {
  .sidebar-header h2 {
    font-size: 16px;
  }
  
  .chat-header h1 {
    font-size: 18px;
  }
  
  .chat-header, .input-container {
    padding: 16px;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .message-content {
    font-size: 14px;
    padding: 12px 16px;
  }
  
  .header-actions {
    flex-direction: column;
  }
}

/* 修复元素堆叠问题 */
.chat-container * {
  box-sizing: border-box;
}

.messages-container {
  position: relative;
  z-index: 1;
}

.input-container {
  position: relative;
  z-index: 2;
}

/* 确保滚动条不影响布局 */
.messages-container {
  scrollbar-gutter: stable;
}
</style>
