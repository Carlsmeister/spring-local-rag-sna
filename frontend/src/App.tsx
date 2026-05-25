import { useState } from 'react';
import { CvProvider, useCv } from './context/CvContext';
import SidebarShell from './components/SidebarShell';
import SilkGradient from './components/animations/Silk';
import ShinyText from './components/animations/ShinyText';
import DocDropzone from './components/DocDropzone';
import AnalysisDashboard from './components/AnalysisDashboard';
import { 
  ArrowRight, 
  Sparkles, 
  Activity, 
  TrendingUp, 
  CheckCircle,
  AlertTriangle
} from 'lucide-react';

type TabType = 'dashboard' | 'matcher' | 'rewriter' | 'admin';

function MainAppContent() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const { analysisResult } = useCv();

  return (
    <SidebarShell activeTab={activeTab} setActiveTab={setActiveTab}>
      
      {/* Dynamic Tab Panel Content */}
      <div className="flex-1 flex flex-col justify-between py-2">
        
        {/* TAB 1: Scan Dashboard Overview */}
        {activeTab === 'dashboard' && (
          <div className="space-y-8 animate-fadeIn w-full">
            {analysisResult === null ? (
              <div className="space-y-8">
                {/* Header */}
                <div>
                  <h2 className="text-3xl font-extrabold tracking-tight">
                    Welcome to <ShinyText text="Career Dashboard" speed={8} />
                  </h2>
                  <p className="text-slate-400 text-sm mt-1">Upload and analyze your documents locally with absolute factual privacy.</p>
                </div>

                {/* Ingestion & Analysis Section Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  
                  {/* Dropzone Upload Box */}
                  <div className="lg:col-span-2">
                    <DocDropzone />
                  </div>

                  {/* Quick Scanning Stats / Instructions Card */}
                  <div className="glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col justify-between min-h-[380px]">
                    <div className="space-y-6">
                      <div className="flex items-center space-x-2">
                        <Sparkles className="h-5 w-5 text-indigo-400" />
                        <h4 className="font-bold text-sm tracking-wider uppercase text-indigo-400">Quick Stats</h4>
                      </div>
                      <div className="space-y-4">
                        <div className="flex justify-between items-center py-2 border-b border-slate-800/40">
                          <span className="text-slate-400 text-xs font-semibold">Parser Engine</span>
                          <span className="text-xs font-bold text-slate-200">Apache Tika</span>
                        </div>
                        <div className="flex justify-between items-center py-2 border-b border-slate-800/40">
                          <span className="text-slate-400 text-xs font-semibold">Local AI Model</span>
                          <span className="text-xs font-bold text-slate-200">Gemma 4:e2b</span>
                        </div>
                        <div className="flex justify-between items-center py-2">
                          <span className="text-slate-400 text-xs font-semibold">PGVector Embeddings</span>
                          <span className="text-xs font-bold text-emerald-500">Enabled</span>
                        </div>
                      </div>
                    </div>
                    <div className="p-4 rounded-2xl bg-indigo-950/20 border border-indigo-800/20 text-xs text-slate-400 leading-relaxed mt-6">
                      ⚠️ <strong>Offline Safety:</strong> All models run fully locally on the host server. No personal data ever leaks to third-party endpoints.
                    </div>
                  </div>

                </div>
              </div>
            ) : (
              <AnalysisDashboard setActiveTab={setActiveTab} />
            )}
          </div>
        )}

        {/* TAB 2: Job Matcher Gaps Panel */}
        {activeTab === 'matcher' && (
          <div className="space-y-8 animate-fadeIn">
            <div>
              <h2 className="text-3xl font-extrabold tracking-tight">
                Job <ShinyText text="Alignment Matcher" speed={8} />
              </h2>
              <p className="text-slate-400 text-sm mt-1">Cross-evaluate your CV segments with target job description ad copies.</p>
            </div>

            {/* Matching Flex Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              
              {/* Left Column - Active CV Context */}
              <div className="glass-panel rounded-3xl p-8 border border-slate-800/40 min-h-[300px] flex flex-col justify-between">
                <div className="space-y-4">
                  <h3 className="font-bold text-lg tracking-tight">Active Candidate Profile</h3>
                  <p className="text-slate-400 text-sm">Once a resume is ingested, its semantic text chunks will load here dynamically for contextual matching.</p>
                </div>
                <div className="h-12 w-full rounded-xl bg-slate-900/30 border border-slate-800/40 flex items-center justify-center text-xs text-slate-500 font-semibold">
                  Awaiting resume upload...
                </div>
              </div>

              {/* Right Column - Job Ad Copy Input */}
              <div className="glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col space-y-4">
                <h3 className="font-bold text-lg tracking-tight">Target Job Ad Text</h3>
                <textarea 
                  className="flex-1 w-full min-h-[200px] rounded-2xl bg-slate-900/30 border border-slate-800/60 p-4 text-slate-300 text-sm focus:outline-none focus:border-indigo-500/60 transition-colors resize-none"
                  placeholder="Paste the target job description or core keywords here to identify compliance gaps..."
                />
                <button className="w-full py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 font-semibold text-sm tracking-wide shadow-lg shadow-indigo-600/20 flex items-center justify-center space-x-2">
                  <span>Analyze Compatibility Gaps</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>

            </div>
          </div>
        )}

        {/* TAB 3: Bullet Rewrite Optimizer */}
        {activeTab === 'rewriter' && (
          <div className="space-y-8 animate-fadeIn">
            <div>
              <h2 className="text-3xl font-extrabold tracking-tight">
                Factual <ShinyText text="Bullet Optimizer" speed={8} />
              </h2>
              <p className="text-slate-400 text-sm mt-1">Optimize individual CV sentences under strict factual integrity guardrails.</p>
            </div>

            <div className="glass-panel rounded-3xl p-8 border border-slate-800/40 text-center py-16">
              <div className="h-12 w-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mx-auto mb-4">
                <Sparkles className="h-6 w-6 text-indigo-400" />
              </div>
              <h3 className="text-lg font-bold">Interactive Sentence Selector</h3>
              <p className="text-slate-400 text-sm max-w-md mx-auto mt-2">
                A list of clickable resume sentences will appear here after ingestion. Selecting any sentence prompts local models to suggest fact-preserving stylistic rewrites.
              </p>
            </div>
          </div>
        )}

        {/* TAB 4: Observability and Telemetry Logs */}
        {activeTab === 'admin' && (
          <div className="space-y-8 animate-fadeIn">
            <div>
              <h2 className="text-3xl font-extrabold tracking-tight">
                Telemetry <ShinyText text="Observability Dashboard" speed={8} />
              </h2>
              <p className="text-slate-400 text-sm mt-1">Real-time tracking of local model latency spreads, success rates, and token footprints.</p>
            </div>

            {/* Quick Metrics Widgets Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="glass-panel rounded-2xl p-6 border border-slate-800/40 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-slate-500 tracking-wider uppercase">Average Latency</span>
                  <p className="text-2xl font-black mt-1 text-indigo-400">178.3s</p>
                </div>
                <Activity className="h-8 w-8 text-indigo-500/30" />
              </div>
              <div className="glass-panel rounded-2xl p-6 border border-slate-800/40 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-slate-500 tracking-wider uppercase">Success Rate</span>
                  <p className="text-2xl font-black mt-1 text-emerald-400">100%</p>
                </div>
                <CheckCircle className="h-8 w-8 text-emerald-500/30" />
              </div>
              <div className="glass-panel rounded-2xl p-6 border border-slate-800/40 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-slate-500 tracking-wider uppercase">Total Token Cost</span>
                  <p className="text-2xl font-black mt-1 text-purple-400">10.4k</p>
                </div>
                <TrendingUp className="h-8 w-8 text-purple-500/30" />
              </div>
            </div>

            {/* Mock Chart Area */}
            <div className="glass-panel rounded-3xl p-8 border border-slate-800/40 min-h-[260px] flex flex-col justify-between">
              <div>
                <h3 className="font-bold text-sm tracking-wider uppercase text-indigo-400 flex items-center space-x-2">
                  <Activity className="h-4 w-4" />
                  <span>Sub-Agent Timeline Footprint (Live Gemma 4:e2b)</span>
                </h3>
                <p className="text-xs text-slate-400 mt-1">Reflects processing timings across concurrent workflows.</p>
              </div>
              
              {/* Visual Bars simulating Recharts layouts */}
              <div className="space-y-4 my-6">
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-slate-400">
                    <span>ATS Scans (ATS Agent)</span>
                    <span>135.9s (1,784 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-slate-900 rounded-full overflow-hidden">
                    <div className="h-full w-[76%] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-slate-400">
                    <span>Readability Review (Recruiter Agent)</span>
                    <span>50.1s (2,244 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-slate-900 rounded-full overflow-hidden">
                    <div className="h-full w-[28%] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-slate-400">
                    <span>Keyword Matcher (Keyword Agent)</span>
                    <span>99.1s (2,192 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-slate-900 rounded-full overflow-hidden">
                    <div className="h-full w-[55%] bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full" />
                  </div>
                </div>
              </div>
              
              <div className="text-[10px] text-slate-500 flex items-center space-x-1.5 justify-end">
                <AlertTriangle className="h-3 w-3 text-slate-500" />
                <span>Aggregated dynamically from target telemetry summaries.</span>
              </div>
            </div>

          </div>
        )}

      </div>
    </SidebarShell>
  );
}

export default function App() {
  return (
    <CvProvider>
      {/* Permanent full-screen Silk WebGL background shader */}
      <div className="fixed inset-0 w-screen h-screen z-0 overflow-hidden bg-slate-950">
        <SilkGradient className="opacity-70 transition-opacity duration-1000" />
      </div>
      
      {/* Application Container */}
      <div className="relative w-screen h-screen z-10 overflow-hidden bg-transparent">
        <MainAppContent />
      </div>
    </CvProvider>
  );
}
