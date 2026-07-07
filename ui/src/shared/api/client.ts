import axios, { AxiosError } from 'axios'
import type { ApiError } from '@shared/types'

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    const apiError = error.response?.data
    const message = apiError?.message ?? error.message ?? 'Network error'
    return Promise.reject(new Error(message))
  },
)
