import axios, { AxiosError } from 'axios'
import type { ApiError } from '@shared/types'

export class HttpError extends Error {
  readonly status: number | null

  constructor(message: string, status: number | null) {
    super(message)
    this.name = 'HttpError'
    this.status = status
  }
}

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    const apiError = error.response?.data
    const message = apiError?.message ?? error.message ?? 'Network error'
    return Promise.reject(new HttpError(message, error.response?.status ?? null))
  },
)
