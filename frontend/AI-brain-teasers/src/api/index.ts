import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
})

export const getRooms = async () => {
  const { data } = await api.get('/rooms')
  return data as Array<{ roomId: number; chatMessageList: Array<any> }>
}

export const postChat = async (roomId: number, userPrompt: string) => {
  const { data } = await api.post(`/${roomId}/chat`, null, { params: { userPrompt } })
  return data as string
}

export default api
