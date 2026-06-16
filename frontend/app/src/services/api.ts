import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

const api = axios.create({ baseURL: API_BASE_URL });

// Attach JWT from Zustand's persisted storage on every request
api.interceptors.request.use((config) => {
  try {
    const raw = localStorage.getItem('auth-storage');
    if (raw) {
      const token = JSON.parse(raw)?.state?.token as string | undefined;
      if (token) config.headers.Authorization = `Bearer ${token}`;
    }
  } catch {
    // ignore parse errors
  }
  return config;
});

// ─── Backend DTO types ────────────────────────────────────────────────────────

export interface AuthResponseDto {
  token: string;
  refreshToken: string;
  userId: string;
  email: string;
  name: string;
  role: string;
  avatarUrl: string | null;
}

export interface UserDto {
  id: string;
  name: string;
  email: string;
  role: 'STUDENT' | 'TEACHER' | 'ADMIN';
  provider: 'LOCAL' | 'GOOGLE';
  avatarUrl: string | null;
  bio: string | null;
  createdAt: string;
}

export interface CourseDto {
  id: string;
  title: string;
  description: string;
  thumbnailUrl: string | null;
  price: number;
  isFree: boolean;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  category: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  totalLessons: number;
  totalDurationMinutes: number;
}

export interface CreateCourseRequest {
  title: string;
  description?: string;
  thumbnailUrl?: string;
  price: number;
  isFree: boolean;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  category: string;
  teacherId: string;
}

export interface UpdateCourseRequest {
  title: string;
  description?: string;
  thumbnailUrl?: string;
  price: number;
  isFree: boolean;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  category: string;
}

export interface ModuleDto {
  id: string;
  title: string;
  orderIndex: number;
}

export interface CreateModuleRequest {
  title: string;
  orderIndex?: number;
}

export interface UpdateModuleRequest {
  title: string;
  orderIndex?: number;
}

export interface LessonDto {
  id: string;
  title: string;
  type: 'VIDEO' | 'TEXT' | 'QUIZ';
  videoUrl: string | null;
  content: string | null;
  durationMinutes: number | null;
  orderIndex: number;
  freePreview: boolean;
}

export interface CreateLessonRequest {
  title: string;
  type: 'VIDEO' | 'TEXT' | 'QUIZ';
  videoUrl?: string;
  content?: string;
  durationMinutes?: number;
  orderIndex?: number;
  freePreview?: boolean;
}

export interface UpdateLessonRequest {
  title: string;
  type: 'VIDEO' | 'TEXT' | 'QUIZ';
  videoUrl?: string;
  content?: string;
  durationMinutes?: number;
  orderIndex?: number;
  freePreview?: boolean;
}

export interface LessonCurriculumDto {
  id: string;
  title: string;
  type: 'VIDEO' | 'TEXT' | 'QUIZ';
  durationMinutes: number | null;
  orderIndex: number;
  freePreview: boolean;
  accessible: boolean;
  videoUrl: string | null;
}

export interface ModuleCurriculumDto {
  id: string;
  title: string;
  description: string | null;
  orderIndex: number;
  lessons: LessonCurriculumDto[];
}

export interface CurriculumDto {
  courseTitle: string;
  enrolled: boolean;
  modules: ModuleCurriculumDto[];
}

export interface UpdateProfileRequest {
  name: string;
  bio?: string;
  avatarUrl?: string;
}

// ─── Generic API response wrapper ─────────────────────────────────────────────

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// ─── Auth API ─────────────────────────────────────────────────────────────────

export const authApi = {
  login: (email: string, password: string) =>
    api.post<ApiResponse<AuthResponseDto>>('/api/auth/login', { email, password }),

  register: (name: string, email: string, password: string) =>
    api.post<ApiResponse<AuthResponseDto>>('/api/auth/register/local', {
      name,
      email,
      password,
    }),
};

// ─── User API ─────────────────────────────────────────────────────────────────

export const userApi = {
  /** GET /v1/users/me — own profile */
  me: () => api.get<ApiResponse<UserDto>>('/v1/users/me'),

  /** PUT /v1/users/me — update own profile */
  updateMe: (data: UpdateProfileRequest) =>
    api.put<ApiResponse<UserDto>>('/v1/users/me', data),

  /** GET /v1/users/:id — public profile */
  getById: (id: string) => api.get<ApiResponse<UserDto>>(`/v1/users/${id}`),
};

// ─── Course API ───────────────────────────────────────────────────────────────

export const courseApi = {
  /** GET /api/v1/courses — all published courses (public) */
  list: () => api.get<ApiResponse<CourseDto[]>>('/api/v1/courses'),

  /** GET /api/v1/courses/:id */
  get: (id: string) => api.get<ApiResponse<CourseDto>>(`/api/v1/courses/${id}`),

  /** POST /api/v1/courses */
  create: (data: CreateCourseRequest) =>
    api.post<ApiResponse<CourseDto>>('/api/v1/courses', data),

  /** PUT /api/v1/courses/:id */
  update: (id: string, data: UpdateCourseRequest) =>
    api.put<ApiResponse<CourseDto>>(`/api/v1/courses/${id}`, data),

  /** DELETE /api/v1/courses/:id */
  delete: (id: string) =>
    api.delete<ApiResponse<string>>(`/api/v1/courses/${id}`),

  /** POST /api/v1/courses/:id/publish */
  publish: (id: string) =>
    api.post<ApiResponse<CourseDto>>(`/api/v1/courses/${id}/publish`),
};

// ─── Module API ───────────────────────────────────────────────────────────────

export const moduleApi = {
  /** POST /api/v1/courses/:courseId/modules */
  create: (courseId: string, data: CreateModuleRequest) =>
    api.post<ApiResponse<ModuleDto>>(`/api/v1/courses/${courseId}/modules`, data),

  /** PUT /api/v1/courses/:courseId/modules/:moduleId */
  update: (courseId: string, moduleId: string, data: UpdateModuleRequest) =>
    api.put<ApiResponse<ModuleDto>>(
      `/api/v1/courses/${courseId}/modules/${moduleId}`,
      data
    ),

  /** DELETE /api/v1/courses/:courseId/modules/:moduleId */
  delete: (courseId: string, moduleId: string) =>
    api.delete<ApiResponse<string>>(
      `/api/v1/courses/${courseId}/modules/${moduleId}`
    ),
};

// ─── Lesson API ───────────────────────────────────────────────────────────────

export const lessonApi = {
  /** POST /api/v1/modules/:moduleId/lessons */
  create: (moduleId: string, data: CreateLessonRequest) =>
    api.post<ApiResponse<LessonDto>>(`/api/v1/modules/${moduleId}/lessons`, data),

  /** GET /api/v1/lessons/:id */
  get: (id: string) => api.get<ApiResponse<LessonDto>>(`/api/v1/lessons/${id}`),

  /** PUT /api/v1/lessons/:id */
  update: (id: string, data: UpdateLessonRequest) =>
    api.put<ApiResponse<LessonDto>>(`/api/v1/lessons/${id}`, data),

  /** DELETE /api/v1/lessons/:id */
  delete: (id: string) =>
    api.delete<ApiResponse<string>>(`/api/v1/lessons/${id}`),
};

// ─── Curriculum API ───────────────────────────────────────────────────────────

export const curriculumApi = {
  /** GET /api/v1/courses/:courseId/curriculum */
  get: (courseId: string) =>
    api.get<ApiResponse<CurriculumDto>>(
      `/api/v1/courses/${courseId}/curriculum`
    ),
};

// ─── Enrollment API ───────────────────────────────────────────────────────────

export const enrollmentApi = {
  /** POST /api/v1/enrollments/courses/:courseId */
  enroll: (courseId: string) =>
    api.post<ApiResponse<void>>(`/api/v1/enrollments/courses/${courseId}`),

  /** GET /api/v1/enrollments/courses/:courseId/status */
  status: (courseId: string) =>
    api.get<ApiResponse<{ enrolled: boolean }>>(
      `/api/v1/enrollments/courses/${courseId}/status`
    ),
};

export default api;