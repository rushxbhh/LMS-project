import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi, courseApi, userApi } from '@/services/api';
import type {
  User,
  Course,
  CourseProgress,
  Notification,
  Badge,
  LeaderboardEntry,
  Activity,
  Deadline,
  DashboardStats,
  QuizAttempt,
} from '@/types';
import type { CourseDto, UserDto } from '@/services/api';

// ─── Mapping helpers ──────────────────────────────────────────────────────────

function courseDtoToFrontend(dto: CourseDto): Course {
  return {
    id: dto.id,
    title: dto.title,
    description: dto.description ?? '',
    thumbnail:
      dto.thumbnailUrl ??
      'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&h=600&fit=crop',
    instructor: {
      id: 'unknown',
      name: 'Instructor',
      avatar: 'https://i.pravatar.cc/150?u=instructor',
      bio: '',
    },
    category: dto.category,
    tags: [dto.category],
    modules: [],
    duration: dto.totalDurationMinutes ?? 0,
    level: (dto.level?.toLowerCase() ?? 'beginner') as
      | 'beginner'
      | 'intermediate'
      | 'advanced',
    price: Number(dto.price ?? 0),
    rating: 0,
    enrolledCount: 0,
    createdAt: new Date(),
    updatedAt: new Date(),
  };
}

function userDtoToFrontend(dto: UserDto): User {
  const parts = (dto.name ?? '').split(' ');
  return {
    id: dto.id,
    email: dto.email,
    firstName: parts[0] ?? '',
    lastName: parts.slice(1).join(' ') ?? '',
    avatar: dto.avatarUrl ?? undefined,
    role: (dto.role?.toLowerCase() ?? 'student') as 'student' | 'teacher' | 'admin',
    createdAt: dto.createdAt ? new Date(dto.createdAt) : new Date(),
    updatedAt: new Date(),
  };
}

// ─── Auth Store ───────────────────────────────────────────────────────────────

interface RegisterData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: 'student' | 'teacher';
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (data: RegisterData) => Promise<void>;
  updateUser: (user: Partial<User>) => void;
  /** Refresh own profile from the API */
  refreshProfile: () => Promise<void>;
  /** Update own profile via API */
  saveProfile: (name: string, bio?: string, avatarUrl?: string) => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,

      login: async (email, password) => {
        set({ isLoading: true });
        try {
          const { data: res } = await authApi.login(email, password);
          const dto = res.data;
          const parts = dto.name.split(' ');
          set({
            token: dto.token,
            isAuthenticated: true,
            isLoading: false,
            user: {
              id: dto.userId,
              email: dto.email,
              firstName: parts[0] ?? dto.name,
              lastName: parts.slice(1).join(' ') ?? '',
              role: (dto.role.toLowerCase()) as 'student' | 'teacher' | 'admin',
              avatar: dto.avatarUrl ?? undefined,
              createdAt: new Date(),
              updatedAt: new Date(),
            },
          });
        } catch (err) {
          set({ isLoading: false });
          throw err;
        }
      },

      logout: () => {
        set({ user: null, token: null, isAuthenticated: false });
      },

      register: async (data) => {
        set({ isLoading: true });
        try {
          const { data: res } = await authApi.register(
            `${data.firstName} ${data.lastName}`,
            data.email,
            data.password
          );
          const dto = res.data;
          set({
            token: dto.token,
            isAuthenticated: true,
            isLoading: false,
            user: {
              id: dto.userId,
              email: dto.email,
              firstName: data.firstName,
              lastName: data.lastName,
              role: (dto.role.toLowerCase()) as 'student' | 'teacher' | 'admin',
              avatar: dto.avatarUrl ?? undefined,
              createdAt: new Date(),
              updatedAt: new Date(),
            },
          });
        } catch (err) {
          set({ isLoading: false });
          throw err;
        }
      },

      updateUser: (userData) => {
        set((state) => ({
          user: state.user ? { ...state.user, ...userData } : null,
        }));
      },

      refreshProfile: async () => {
        try {
          const { data: res } = await userApi.me();
          set({ user: userDtoToFrontend(res.data) });
        } catch {
          // token may have expired — silently ignore
        }
      },

      saveProfile: async (name, bio, avatarUrl) => {
        const { data: res } = await userApi.updateMe({ name, bio, avatarUrl });
        set({ user: userDtoToFrontend(res.data) });
      },
    }),
    { name: 'auth-storage' }
  )
);

// ─── Course Store ─────────────────────────────────────────────────────────────

interface CourseState {
  courses: Course[];
  currentCourse: Course | null;
  enrolledCourses: string[];
  courseProgress: Record<string, CourseProgress>;
  isLoading: boolean;
  error: string | null;
  fetchCourses: () => Promise<void>;
  fetchCourse: (id: string) => Promise<void>;
  enrollCourse: (courseId: string) => Promise<void>;
  updateProgress: (courseId: string, lessonId: string) => void;
  setCurrentCourse: (course: Course | null) => void;
}

export const useCourseStore = create<CourseState>()(
  persist(
    (set, get) => ({
      courses: [],
      currentCourse: null,
      enrolledCourses: [],
      courseProgress: {},
      isLoading: false,
      error: null,

      fetchCourses: async () => {
        set({ isLoading: true, error: null });
        try {
          const { data: res } = await courseApi.list();
          const courses = res.data.map(courseDtoToFrontend);
          set({ courses, isLoading: false });
        } catch (err: unknown) {
          const msg =
            (err as { response?: { data?: { message?: string } } })?.response?.data
              ?.message ?? 'Failed to load courses';
          set({ isLoading: false, error: msg });
          // fall back to empty rather than crashing
        }
      },

      fetchCourse: async (id) => {
        set({ isLoading: true, error: null });
        try {
          const { data: res } = await courseApi.get(id);
          const course = courseDtoToFrontend(res.data);
          set({ currentCourse: course, isLoading: false });
          // also update the list entry if present
          set((state) => ({
            courses: state.courses.map((c) => (c.id === id ? course : c)),
          }));
        } catch {
          set({ isLoading: false });
        }
      },

      enrollCourse: async (courseId) => {
        const { enrollmentApi } = await import('@/services/api');
        await enrollmentApi.enroll(courseId);
        set((state) => ({
          enrolledCourses: [...new Set([...state.enrolledCourses, courseId])],
        }));
      },

      updateProgress: (courseId, lessonId) => {
        set((state) => {
          const existing = state.courseProgress[courseId] ?? {
            courseId,
            studentId: '',
            completedLessons: [],
            completedModules: [],
            progress: 0,
            lastAccessed: new Date(),
            totalTimeSpent: 0,
          };
          const completedLessons = existing.completedLessons.includes(lessonId)
            ? existing.completedLessons
            : [...existing.completedLessons, lessonId];
          return {
            courseProgress: {
              ...state.courseProgress,
              [courseId]: { ...existing, completedLessons },
            },
          };
        });
      },

      setCurrentCourse: (course) => set({ currentCourse: course }),
    }),
    { name: 'course-storage' }
  )
);

// ─── UI Store ─────────────────────────────────────────────────────────────────

interface UIState {
  sidebarOpen: boolean;
  notificationsOpen: boolean;
  searchOpen: boolean;
  theme: 'light' | 'dark';
  language: string;
  toggleSidebar: () => void;
  toggleNotifications: () => void;
  toggleSearch: () => void;
  setTheme: (theme: 'light' | 'dark') => void;
  setLanguage: (lang: string) => void;
}

export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      sidebarOpen: true,
      notificationsOpen: false,
      searchOpen: false,
      theme: 'light',
      language: 'en',
      toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
      toggleNotifications: () =>
        set((state) => ({ notificationsOpen: !state.notificationsOpen })),
      toggleSearch: () =>
        set((state) => ({ searchOpen: !state.searchOpen })),
      setTheme: (theme) => set({ theme }),
      setLanguage: (language) => set({ language }),
    }),
    { name: 'ui-storage' }
  )
);

// ─── Notification Store ───────────────────────────────────────────────────────

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  fetchNotifications: () => Promise<void>;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
}

export const useNotificationStore = create<NotificationState>()(
  persist(
    (set) => ({
      notifications: [],
      unreadCount: 0,
      fetchNotifications: async () => {
        // Notifications endpoint not yet implemented in backend —
        // use local mock so the rest of the UI works
        const mocks = generateMockNotifications();
        set({
          notifications: mocks,
          unreadCount: mocks.filter((n) => !n.isRead).length,
        });
      },
      markAsRead: (id) => {
        set((state) => ({
          notifications: state.notifications.map((n) =>
            n.id === id ? { ...n, isRead: true } : n
          ),
          unreadCount: Math.max(0, state.unreadCount - 1),
        }));
      },
      markAllAsRead: () => {
        set((state) => ({
          notifications: state.notifications.map((n) => ({ ...n, isRead: true })),
          unreadCount: 0,
        }));
      },
    }),
    { name: 'notification-storage' }
  )
);

// ─── Dashboard Store ──────────────────────────────────────────────────────────

interface DashboardState {
  stats: DashboardStats | null;
  leaderboard: LeaderboardEntry[];
  activities: Activity[];
  deadlines: Deadline[];
  badges: Badge[];
  isLoading: boolean;
  fetchDashboardData: () => Promise<void>;
}

export const useDashboardStore = create<DashboardState>((set) => ({
  stats: null,
  leaderboard: [],
  activities: [],
  deadlines: [],
  badges: [],
  isLoading: false,
  fetchDashboardData: async () => {
    set({ isLoading: true });
    // Dashboard aggregates (streaks, badges, leaderboard) are not yet
    // exposed as API endpoints — use local mocks for now
    await new Promise((r) => setTimeout(r, 600));
    set({
      stats: generateMockStats(),
      leaderboard: generateMockLeaderboard(),
      activities: generateMockActivities(),
      deadlines: generateMockDeadlines(),
      badges: generateMockBadges(),
      isLoading: false,
    });
  },
}));

// ─── Quiz Store ───────────────────────────────────────────────────────────────

interface QuizState {
  currentQuiz: unknown | null;
  quizAttempt: QuizAttempt | null;
  timeRemaining: number;
  isQuizActive: boolean;
  startQuiz: (quiz: { id: string; timeLimit: number; questions: { points: number }[] }) => void;
  submitAnswer: (questionId: string, options: string[]) => void;
  submitQuiz: () => void;
  tickTimer: () => void;
}

export const useQuizStore = create<QuizState>((set) => ({
  currentQuiz: null,
  quizAttempt: null,
  timeRemaining: 0,
  isQuizActive: false,
  startQuiz: (quiz) => {
    set({
      currentQuiz: quiz,
      timeRemaining: quiz.timeLimit * 60,
      isQuizActive: true,
      quizAttempt: {
        id: Date.now().toString(),
        quizId: quiz.id,
        studentId: '',
        score: 0,
        maxScore: quiz.questions.reduce((acc, q) => acc + q.points, 0),
        answers: [],
        startedAt: new Date(),
        completedAt: new Date(),
        timeSpent: 0,
      },
    });
  },
  submitAnswer: (questionId, options) => {
    set((state) => {
      if (!state.quizAttempt) return state;
      const newAnswers = state.quizAttempt.answers.filter(
        (a) => a.questionId !== questionId
      );
      newAnswers.push({ questionId, selectedOptions: options, isCorrect: false });
      return { quizAttempt: { ...state.quizAttempt, answers: newAnswers } };
    });
  },
  submitQuiz: () => set({ isQuizActive: false, currentQuiz: null }),
  tickTimer: () => {
    set((state) => {
      if (state.timeRemaining <= 0) return { isQuizActive: false };
      return { timeRemaining: state.timeRemaining - 1 };
    });
  },
}));

// ─── Mock data (used for features not yet backed by an API endpoint) ──────────

function generateMockNotifications(): Notification[] {
  return [
    {
      id: '1',
      userId: '1',
      type: 'course',
      title: 'New lesson available',
      message: 'A new lesson has been added to one of your courses',
      isRead: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 30),
    },
    {
      id: '2',
      userId: '1',
      type: 'quiz',
      title: 'Quiz reminder',
      message: 'You have a quiz due tomorrow',
      isRead: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2),
    },
    {
      id: '3',
      userId: '1',
      type: 'achievement',
      title: 'New badge earned!',
      message: 'Congratulations! You earned the "Fast Learner" badge.',
      isRead: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24),
    },
  ];
}

function generateMockStats(): DashboardStats {
  return {
    streak: 12,
    coursesInProgress: 3,
    completedCourses: 5,
    averageScore: 87,
    totalLearningTime: 1240,
    certificatesEarned: 3,
  };
}

function generateMockLeaderboard(): LeaderboardEntry[] {
  const makeUser = (id: string, first: string, last: string, u: number): User => ({
    id,
    email: `${first.toLowerCase()}@example.com`,
    firstName: first,
    lastName: last,
    avatar: `https://i.pravatar.cc/150?u=${u}`,
    role: 'student',
    createdAt: new Date(),
    updatedAt: new Date(),
  });
  return [
    { rank: 1, user: makeUser('2', 'Sarah', 'Chen', 20), points: 12500, coursesCompleted: 12, streak: 45 },
    { rank: 2, user: makeUser('3', 'Mike', 'Ross', 21), points: 11200, coursesCompleted: 10, streak: 32 },
    { rank: 3, user: makeUser('4', 'Emma', 'Wilson', 22), points: 10800, coursesCompleted: 9, streak: 28 },
    { rank: 4, user: makeUser('5', 'Alex', 'Johnson', 1), points: 9500, coursesCompleted: 8, streak: 12 },
    { rank: 5, user: makeUser('6', 'James', 'Park', 23), points: 8900, coursesCompleted: 7, streak: 18 },
  ];
}

function generateMockActivities(): Activity[] {
  const makeUser = (id: string, first: string, last: string, u: number): User => ({
    id,
    email: `${first.toLowerCase()}@example.com`,
    firstName: first,
    lastName: last,
    avatar: `https://i.pravatar.cc/150?u=${u}`,
    role: 'student',
    createdAt: new Date(),
    updatedAt: new Date(),
  });
  return [
    {
      id: '1',
      user: makeUser('2', 'Sarah', 'Chen', 20),
      type: 'completion',
      description: 'completed a course',
      timestamp: new Date(Date.now() - 1000 * 60 * 15),
    },
    {
      id: '2',
      user: makeUser('3', 'Mike', 'Ross', 21),
      type: 'enrollment',
      description: 'enrolled in a new course',
      timestamp: new Date(Date.now() - 1000 * 60 * 60),
    },
    {
      id: '3',
      user: makeUser('4', 'Emma', 'Wilson', 22),
      type: 'quiz_pass',
      description: 'scored 95% on a quiz',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
    },
  ];
}

function generateMockDeadlines(): Deadline[] {
  return [
    {
      id: '1',
      title: 'Web Design Assignment',
      courseId: '1',
      courseName: 'Complete Web Design Bootcamp',
      dueDate: new Date(Date.now() + 1000 * 60 * 60 * 24),
      type: 'assignment',
      priority: 'high',
    },
    {
      id: '2',
      title: 'Python Quiz',
      courseId: '2',
      courseName: 'Python for Data Science',
      dueDate: new Date(Date.now() + 1000 * 60 * 60 * 24 * 3),
      type: 'quiz',
      priority: 'medium',
    },
  ];
}

function generateMockBadges(): Badge[] {
  return [
    { id: '1', name: 'Fast Learner', description: 'Complete 5 lessons in a day', icon: 'Zap', criteria: '5 lessons/day', points: 100, isUnlocked: true, unlockedAt: new Date() },
    { id: '2', name: 'Quiz Master', description: 'Score 100% on 3 quizzes', icon: 'Target', criteria: '3 perfect quizzes', points: 200, isUnlocked: true, unlockedAt: new Date() },
    { id: '3', name: 'Consistent', description: '7-day streak', icon: 'Flame', criteria: '7 days', points: 150, isUnlocked: true },
    { id: '4', name: 'Helpful', description: 'Answer 10 questions', icon: 'Heart', criteria: '10 answers', points: 100, isUnlocked: false },
    { id: '5', name: 'Top Performer', description: 'Top 10 leaderboard', icon: 'Trophy', criteria: 'Top 10', points: 500, isUnlocked: false },
  ];
}