import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Play, Clock, Users, Star, Award, CheckCircle, ChevronDown, ChevronUp,
  FileText, Download, Share2, Heart, Lock, Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { useCourseStore } from '@/store';
import { curriculumApi, enrollmentApi } from '@/services/api';
import type { CurriculumDto, LessonCurriculumDto } from '@/services/api';
import { toast } from 'sonner';

// ─── Iframe video modal ───────────────────────────────────────────────────────

function VideoModal({
  lesson,
  onClose,
}: {
  lesson: LessonCurriculumDto;
  onClose: () => void;
}) {
  return (
    <div
      className="fixed inset-0 bg-black/80 z-50 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        className="w-full max-w-4xl bg-black rounded-2xl overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between p-4 bg-[#1A202C] text-white">
          <span className="font-medium">{lesson.title}</span>
          <button onClick={onClose} className="text-white/60 hover:text-white text-2xl leading-none">
            ×
          </button>
        </div>
        <div className="relative" style={{ paddingTop: '56.25%' }}>
          <iframe
            className="absolute inset-0 w-full h-full"
            src={lesson.videoUrl!}
            title={lesson.title}
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        </div>
      </div>
    </div>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

export default function CourseDetail() {
  const { id } = useParams<{ id: string }>();
  const { courses } = useCourseStore();
  const [expandedModules, setExpandedModules] = useState<string[]>([]);
  const [isLiked, setIsLiked] = useState(false);
  const [enrolling, setEnrolling] = useState(false);
  const [playingLesson, setPlayingLesson] = useState<LessonCurriculumDto | null>(null);

  // Curriculum state
  const [curriculum, setCurriculum] = useState<CurriculumDto | null>(null);
  const [curriculumLoading, setCurriculumLoading] = useState(false);

  const course = courses.find((c) => c.id === id);

  // Fetch curriculum on mount
  useEffect(() => {
    if (!id) return;
    setCurriculumLoading(true);
    curriculumApi
      .get(id)
      .then(({ data }) => setCurriculum(data.data))
      .catch(() => toast.error('Could not load course curriculum'))
      .finally(() => setCurriculumLoading(false));
  }, [id]);

  const toggleModule = (moduleId: string) => {
    setExpandedModules((prev) =>
      prev.includes(moduleId) ? prev.filter((m) => m !== moduleId) : [...prev, moduleId]
    );
  };

  const handleEnroll = async () => {
    if (!id) return;
    setEnrolling(true);
    try {
      await enrollmentApi.enroll(id);
      toast.success('Enrolled successfully!');
      // Re-fetch curriculum — now enrolled: true, all videoUrls visible
      const { data } = await curriculumApi.get(id);
      setCurriculum(data.data);
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Enrollment failed';
      toast.error(msg);
    } finally {
      setEnrolling(false);
    }
  };

  const handleLessonClick = (lesson: LessonCurriculumDto) => {
    if (!lesson.accessible) {
      toast.info('Enroll in this course to unlock all lessons');
      return;
    }
    if (lesson.type === 'VIDEO' && lesson.videoUrl) {
      setPlayingLesson(lesson);
    } else {
      toast.info(`${lesson.type.toLowerCase()} lesson`);
    }
  };

  const formatDuration = (minutes: number | null) => {
    if (!minutes) return '-';
    return `${Math.floor(minutes / 60)}:${String(minutes % 60).padStart(2, '0')}`;
  };

  if (!course) {
    return (
      <div className="flex items-center justify-center h-80">
        <Loader2 className="w-8 h-8 animate-spin text-[#0056D1]" />
      </div>
    );
  }

  const enrolled = curriculum?.enrolled ?? false;

  return (
    <div className="space-y-8">
      {/* ── Hero ──────────────────────────────────────────────── */}
      <div className="relative bg-gradient-to-br from-[#002A6C] to-[#0056D1] rounded-3xl overflow-hidden">
        <div className="relative z-10 p-8 lg:p-12">
          <div className="flex flex-col lg:flex-row gap-8 items-start">
            {/* Course info */}
            <div className="flex-1 text-white">
              <div className="flex gap-2 mb-4">
                <Badge className="bg-white/20 text-white">{course.category}</Badge>
                <Badge className="bg-white/20 text-white capitalize">{course.level}</Badge>
              </div>
              <h1 className="text-3xl lg:text-4xl font-bold mb-4">{course.title}</h1>
              <p className="text-white/80 text-lg mb-6 max-w-2xl">{course.description}</p>
              <div className="flex flex-wrap items-center gap-6 text-sm">
                <div className="flex items-center gap-2">
                  <Star className="w-5 h-5 text-yellow-400 fill-yellow-400" />
                  <span className="font-bold">{course.rating}</span>
                  <span className="text-white/60">({course.enrolledCount} reviews)</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="w-5 h-5" />
                  <span>{course.enrolledCount.toLocaleString()} students</span>
                </div>
                <div className="flex items-center gap-2">
                  <Clock className="w-5 h-5" />
                  <span>
                    {Math.floor(course.duration / 60)}h {course.duration % 60}m
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <Award className="w-5 h-5" />
                  <span>Certificate of completion</span>
                </div>
              </div>
              <div className="flex items-center gap-4 mt-6">
                <Avatar className="w-12 h-12 border-2 border-white">
                  <AvatarImage src={course.instructor.avatar} />
                  <AvatarFallback>{course.instructor.name[0]}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-semibold">{course.instructor.name}</p>
                  <p className="text-sm text-white/60">{course.instructor.bio}</p>
                </div>
              </div>
            </div>

            {/* Enroll card */}
            <Card className="w-full lg:w-80 flex-shrink-0">
              <CardContent className="p-6">
                <div className="aspect-video rounded-xl overflow-hidden mb-4 relative">
                  <img
                    src={course.thumbnail}
                    alt={course.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 flex items-center justify-center bg-black/30">
                    <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-lg">
                      <Play className="w-6 h-6 text-[#0056D1] ml-1" />
                    </div>
                  </div>
                </div>

                <div className="flex items-baseline gap-2 mb-4">
                  <span className="text-3xl font-bold text-[#1A202C]">${course.price}</span>
                  {enrolled && (
                    <Badge className="bg-green-500">Enrolled ✓</Badge>
                  )}
                </div>

                {enrolled ? (
                  <Link to={`/learning/${course.id}`}>
                    <Button className="w-full h-12 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl mb-3">
                      Go to Course
                    </Button>
                  </Link>
                ) : (
                  <Button
                    className="w-full h-12 bg-[#0056D1] hover:bg-[#002A6C] text-white font-semibold rounded-xl mb-3"
                    onClick={handleEnroll}
                    disabled={enrolling}
                  >
                    {enrolling ? (
                      <Loader2 className="w-5 h-5 animate-spin" />
                    ) : (
                      'Enroll Now'
                    )}
                  </Button>
                )}

                <p className="text-center text-sm text-[#718096] mb-4">
                  30-day money-back guarantee
                </p>

                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    className="flex-1"
                    onClick={() => setIsLiked(!isLiked)}
                  >
                    <Heart
                      className={`w-4 h-4 mr-2 ${isLiked ? 'fill-red-500 text-red-500' : ''}`}
                    />
                    Wishlist
                  </Button>
                  <Button variant="outline" className="flex-1">
                    <Share2 className="w-4 h-4 mr-2" />
                    Share
                  </Button>
                </div>

                <Separator className="my-4" />

                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-[#718096]">Duration</span>
                    <span className="font-medium">{Math.floor(course.duration / 60)}h</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-[#718096]">Skill level</span>
                    <span className="font-medium capitalize">{course.level}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>

      {/* ── Tabs ──────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          <Tabs defaultValue="content" className="w-full">
            <TabsList className="w-full justify-start bg-white p-1 rounded-xl mb-6">
              {(['content', 'overview', 'reviews', 'instructor'] as const).map((t) => (
                <TabsTrigger
                  key={t}
                  value={t}
                  className="rounded-lg capitalize data-[state=active]:bg-[#0056D1] data-[state=active]:text-white"
                >
                  {t}
                </TabsTrigger>
              ))}
            </TabsList>

            {/* ── COURSE CONTENT TAB ── */}
            <TabsContent value="content" className="mt-0">
              <Card>
                <CardContent className="p-6">
                  {/* Header row */}
                  <div className="flex items-center justify-between mb-6">
                    <div>
                      <h3 className="text-xl font-bold text-[#1A202C]">Course Content</h3>
                      {curriculum && (
                        <p className="text-sm text-[#718096]">
                          {curriculum.modules.length} modules •{' '}
                          {curriculum.modules.reduce((a, m) => a + m.lessons.length, 0)} lessons
                        </p>
                      )}
                    </div>

                    {/* Enrollment pill */}
                    {enrolled ? (
                      <Badge className="bg-green-500 px-3 py-1">
                        <CheckCircle className="w-4 h-4 mr-1" />
                        Full access
                      </Badge>
                    ) : (
                      <Badge className="bg-[#E6F0FF] text-[#0056D1] px-3 py-1">
                        <Lock className="w-4 h-4 mr-1" />
                        Preview mode
                      </Badge>
                    )}
                  </div>

                  {/* Loading state */}
                  {curriculumLoading && (
                    <div className="flex justify-center py-12">
                      <Loader2 className="w-8 h-8 animate-spin text-[#0056D1]" />
                    </div>
                  )}

                  {/* Curriculum tree */}
                  {!curriculumLoading && curriculum && (
                    <div className="space-y-3">
                      {curriculum.modules.map((module) => (
                        <div
                          key={module.id}
                          className="border border-[#E6F0FF] rounded-xl overflow-hidden"
                        >
                          {/* Module header */}
                          <button
                            onClick={() => toggleModule(module.id)}
                            className="w-full flex items-center justify-between p-4 hover:bg-[#E6F0FF]/50 transition-colors"
                          >
                            <span className="font-medium text-[#1A202C]">{module.title}</span>
                            <div className="flex items-center gap-3">
                              <span className="text-sm text-[#718096]">
                                {module.lessons.length} lessons
                              </span>
                              {expandedModules.includes(module.id) ? (
                                <ChevronUp className="w-5 h-5 text-[#718096]" />
                              ) : (
                                <ChevronDown className="w-5 h-5 text-[#718096]" />
                              )}
                            </div>
                          </button>

                          {/* Lessons */}
                          {expandedModules.includes(module.id) && (
                            <div className="border-t border-[#E6F0FF] bg-[#F8FAFC]">
                              {module.lessons.map((lesson) => (
                                <button
                                  key={lesson.id}
                                  onClick={() => handleLessonClick(lesson)}
                                  className={`w-full flex items-center justify-between p-4 transition-colors text-left ${
                                    lesson.accessible
                                      ? 'hover:bg-white cursor-pointer'
                                      : 'cursor-not-allowed opacity-60'
                                  }`}
                                >
                                  <div className="flex items-center gap-3">
                                    {/* Icon */}
                                    {lesson.accessible ? (
                                      lesson.type === 'VIDEO' ? (
                                        <div className="w-8 h-8 bg-[#0056D1] rounded-full flex items-center justify-center">
                                          <Play className="w-4 h-4 text-white ml-0.5" />
                                        </div>
                                      ) : lesson.type === 'TEXT' ? (
                                        <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center">
                                          <FileText className="w-4 h-4 text-white" />
                                        </div>
                                      ) : (
                                        <div className="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">
                                          <CheckCircle className="w-4 h-4 text-white" />
                                        </div>
                                      )
                                    ) : (
                                      <div className="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center">
                                        <Lock className="w-4 h-4 text-gray-500" />
                                      </div>
                                    )}

                                    <div>
                                      <p className="text-sm font-medium text-[#1A202C]">
                                        {lesson.title}
                                      </p>
                                      <div className="flex items-center gap-2 mt-0.5">
                                        <span className="text-xs text-[#718096] capitalize">
                                          {lesson.type.toLowerCase()}
                                        </span>
                                        {lesson.freePreview && (
                                          <Badge className="text-xs px-1 py-0 bg-orange-100 text-orange-600 border-0">
                                            Preview
                                          </Badge>
                                        )}
                                      </div>
                                    </div>
                                  </div>

                                  <span className="text-xs text-[#718096]">
                                    {formatDuration(lesson.durationMinutes)}
                                  </span>
                                </button>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Enroll CTA at bottom of locked curriculum */}
                  {!curriculumLoading && curriculum && !enrolled && (
                    <div className="mt-6 p-4 bg-[#E6F0FF] rounded-xl text-center">
                      <Lock className="w-8 h-8 text-[#0056D1] mx-auto mb-2" />
                      <p className="text-sm text-[#718096] mb-3">
                        Enroll to unlock all{' '}
                        {curriculum.modules.reduce((a, m) => a + m.lessons.length, 0)} lessons
                      </p>
                      <Button
                        className="bg-[#0056D1]"
                        onClick={handleEnroll}
                        disabled={enrolling}
                      >
                        {enrolling ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          'Enroll Now'
                        )}
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            {/* ── OVERVIEW / REVIEWS / INSTRUCTOR tabs unchanged ── */}
            <TabsContent value="overview" className="mt-0">
              <Card>
                <CardContent className="p-6">
                  <h3 className="text-xl font-bold text-[#1A202C] mb-3">Description</h3>
                  <p className="text-[#718096]">{course.description}</p>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="reviews" className="mt-0">
              <Card>
                <CardContent className="p-6">
                  <div className="text-center py-8">
                    <Star className="w-12 h-12 text-yellow-400 mx-auto mb-2" />
                    <p className="text-4xl font-bold">{course.rating}</p>
                    <p className="text-[#718096]">Course Rating</p>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="instructor" className="mt-0">
              <Card>
                <CardContent className="p-6">
                  <div className="flex items-start gap-4">
                    <Avatar className="w-20 h-20">
                      <AvatarImage src={course.instructor.avatar} />
                      <AvatarFallback>{course.instructor.name[0]}</AvatarFallback>
                    </Avatar>
                    <div>
                      <h3 className="text-xl font-bold">{course.instructor.name}</h3>
                      <p className="text-[#718096]">{course.instructor.bio}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <Card>
            <CardContent className="p-6">
              <h3 className="font-bold text-[#1A202C] mb-4">This course includes:</h3>
              <ul className="space-y-3">
                {[
                  { icon: Play, text: `${Math.floor(course.duration / 60)}h on-demand video` },
                  { icon: FileText, text: 'Downloadable resources' },
                  { icon: Award, text: 'Certificate of completion' },
                  { icon: Download, text: 'Lifetime access' },
                ].map((item, i) => (
                  <li key={i} className="flex items-center gap-3 text-sm text-[#718096]">
                    <item.icon className="w-5 h-5 text-[#0056D1]" />
                    {item.text}
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* ── Video modal ──────────────────────────────────────── */}
      {playingLesson && (
        <VideoModal lesson={playingLesson} onClose={() => setPlayingLesson(null)} />
      )}
    </div>
  );
}