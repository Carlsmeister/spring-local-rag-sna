import { useState } from 'react';
import { CvProvider, useCv } from './context/CvContext';
import SidebarShell from './components/SidebarShell';
import DocDropzone from './components/DocDropzone';
import AnalysisDashboard from './components/AnalysisDashboard';
import { 
  ArrowRight, 
  Sparkles, 
  Activity, 
  TrendingUp, 
  CheckCircle,
  AlertTriangle,
  ShieldAlert
} from 'lucide-react';

type TabType = 'dashboard' | 'matcher' | 'rewriter' | 'admin';

function MainAppContent() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const { analysisResult } = useCv();

  return (
    <SidebarShell activeTab={activeTab} setActiveTab={setActiveTab}>
      
      {/* Dynamic Tab Panel Content */}
      <div className="flex-1 flex flex-col justify-between py-2">
        
        {/* TAB 1: CV Review Overview */}
        {activeTab === 'dashboard' && (
          <div className="space-y-8 animate-fadeIn w-full">
            {analysisResult === null ? (
              <div className="space-y-6">
                {/* Header */}
                <div className="space-y-1">
                  <h2 className="text-2xl font-bold tracking-tight text-[var(--color-primary)]">
                    CV Review
                  </h2>
                  <p className="text-[var(--color-text-secondary)] text-sm">
                    Upload a PDF or DOCX file to review its structure, clarity, and compatibility.
                  </p>
                </div>

                {/* Ingestion & Analysis Section Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  
                  {/* Dropzone Upload Box */}
                  <div className="lg:col-span-2">
                    <DocDropzone />
                  </div>

                  {/* Quick Scanning Stats / Instructions Card */}
                  <div className="glass-panel rounded-xl p-8 flex flex-col justify-between min-h-[380px]">
                    <div className="space-y-6">
                      <div className="flex items-center space-x-2">
                        <Sparkles className="h-5 w-5 text-[var(--color-accent-dark)]" />
                        <h4 className="font-bold text-xs tracking-wider uppercase text-[var(--color-accent-dark)]">Workspace Info</h4>
                      </div>
                      <div className="space-y-4">
                        <div className="flex justify-between items-center py-2 border-b border-[var(--color-border)]">
                          <span className="text-[var(--color-text-secondary)] text-xs font-medium">Text Parser</span>
                          <span className="text-xs font-bold text-[var(--color-text-primary)]">Secure Document Extractor</span>
                        </div>
                        <div className="flex justify-between items-center py-2 border-b border-[var(--color-border)]">
                          <span className="text-[var(--color-text-secondary)] text-xs font-medium">Local AI Model</span>
                          <span className="text-xs font-bold text-[var(--color-success)]">Private Inference Ready</span>
                        </div>
                        <div className="flex justify-between items-center py-2">
                          <span className="text-[var(--color-text-secondary)] text-xs font-medium">Memory Indexing</span>
                          <span className="text-xs font-bold text-[var(--color-accent-dark)]">Local PGVector</span>
                        </div>
                      </div>
                    </div>
                    <div className="p-4 rounded-lg bg-[var(--color-warning-subtle)] border border-[var(--color-warning)] text-xs text-[var(--color-text-primary)] leading-relaxed mt-6 flex items-start space-x-2">
                      <ShieldAlert className="h-4 w-4 text-[var(--color-warning)] flex-shrink-0 mt-0.5" />
                      <span>
                        <strong>Offline Safety:</strong> All processing runs locally on this device. No personal data or document content ever leaves your machine.
                      </span>
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
          <div className="space-y-6 animate-fadeIn">
            <div className="space-y-1">
              <h2 className="text-2xl font-bold tracking-tight text-[var(--color-primary)]">
                Job Match
              </h2>
              <p className="text-[var(--color-text-secondary)] text-sm">
                Cross-evaluate your CV segments with target job description ad copies.
              </p>
            </div>

            {/* Matching Flex Layout */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              
              {/* Left Column - Active CV Context */}
              <div className="glass-panel rounded-xl p-8 min-h-[300px] flex flex-col justify-between">
                <div className="space-y-4">
                  <h3 className="font-bold text-base text-[var(--color-text-primary)]">Active Candidate Profile</h3>
                  <p className="text-[var(--color-text-secondary)] text-xs leading-relaxed">
                    Once a resume is ingested, its semantic text chunks will load here dynamically for contextual matching.
                  </p>
                </div>
                <div className="h-12 w-full rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] flex items-center justify-center text-xs text-[var(--color-text-muted)] font-semibold">
                  Awaiting resume upload...
                </div>
              </div>

              {/* Right Column - Job Ad Copy Input */}
              <div className="glass-panel rounded-xl p-8 flex flex-col space-y-4">
                <h3 className="font-bold text-base text-[var(--color-text-primary)]">Target Job Description</h3>
                <textarea 
                  className="flex-grow w-full min-h-[200px] rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] p-4 text-[var(--color-text-primary)] text-sm focus:outline-none focus:border-[var(--color-border-strong)] transition-colors resize-none"
                  placeholder="Paste the target job advertisement text or core requirements here to compare..."
                />
                <button className="w-full py-3 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-sm tracking-wide shadow-sm flex items-center justify-center space-x-2 transition-all duration-200">
                  <span>Analyse job match</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>

            </div>
          </div>
        )}

        {/* TAB 3: Bullet Rewrite Optimizer */}
        {activeTab === 'rewriter' && (
          <div className="space-y-6 animate-fadeIn">
            <div className="space-y-1">
              <h2 className="text-2xl font-bold tracking-tight text-[var(--color-primary)]">
                Bullet Optimizer
              </h2>
              <p className="text-[var(--color-text-secondary)] text-sm">
                Optimize individual CV sentences under strict factual integrity guardrails.
              </p>
            </div>

            <div className="glass-panel rounded-xl p-8 text-center py-16">
              <div className="h-12 w-12 rounded-xl bg-[var(--color-accent-subtle)] border border-[var(--color-accent)] flex items-center justify-center mx-auto mb-4">
                <Sparkles className="h-6 w-6 text-[var(--color-accent-dark)]" />
              </div>
              <h3 className="text-base font-bold text-[var(--color-text-primary)]">Interactive Sentence Selector</h3>
              <p className="text-[var(--color-text-secondary)] text-xs max-w-md mx-auto mt-2 leading-relaxed">
                A list of clickable resume sentences will appear here after ingestion. Selecting any sentence prompts local models to suggest fact-preserving stylistic rewrites.
              </p>
            </div>
          </div>
        )}

        {/* TAB 4: Observability and Telemetry Logs */}
        {activeTab === 'admin' && (
          <div className="space-y-6 animate-fadeIn">
            <div className="space-y-1">
              <h2 className="text-2xl font-bold tracking-tight text-[var(--color-primary)]">
                System Monitor
              </h2>
              <p className="text-[var(--color-text-secondary)] text-sm">
                Real-time tracking of local model latency spreads, success rates, and token footprints.
              </p>
            </div>

            {/* Quick Metrics Widgets Grid */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="glass-panel rounded-xl p-6 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-[var(--color-text-secondary)] tracking-wider uppercase">Average Latency</span>
                  <p className="text-xl font-extrabold mt-1 text-[var(--color-primary)]">178.3s</p>
                </div>
                <Activity className="h-8 w-8 text-[var(--color-text-muted)] opacity-40" />
              </div>
              <div className="glass-panel rounded-xl p-6 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-[var(--color-text-secondary)] tracking-wider uppercase">Success Rate</span>
                  <p className="text-xl font-extrabold mt-1 text-[var(--color-success)]">100%</p>
                </div>
                <CheckCircle className="h-8 w-8 text-[var(--color-success)] opacity-30" />
              </div>
              <div className="glass-panel rounded-xl p-6 flex items-center justify-between">
                <div>
                  <span className="text-[10px] font-bold text-[var(--color-text-secondary)] tracking-wider uppercase">Total Token Cost</span>
                  <p className="text-xl font-extrabold mt-1 text-[var(--color-primary)]">10.4k</p>
                </div>
                <TrendingUp className="h-8 w-8 text-[var(--color-text-muted)] opacity-40" />
              </div>
            </div>

            {/* Mock Chart Area */}
            <div className="glass-panel rounded-xl p-8 min-h-[260px] flex flex-col justify-between">
              <div>
                <h3 className="font-bold text-xs tracking-wider uppercase text-[var(--color-primary)] flex items-center space-x-2">
                  <Activity className="h-4 w-4" />
                  <span>Sub-Agent Timeline Footprint</span>
                </h3>
                <p className="text-[11px] text-[var(--color-text-secondary)] mt-1">Reflects processing timings across concurrent workflows.</p>
              </div>
              
              {/* Visual Bars simulating layout */}
              <div className="space-y-4 my-6">
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-[var(--color-text-secondary)]">
                    <span>ATS Scans</span>
                    <span>135.9s (1,784 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-[var(--color-surface-hover)] rounded-full overflow-hidden">
                    <div className="h-full w-[76%] bg-[var(--color-primary)] rounded-full" />
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-[var(--color-text-secondary)]">
                    <span>Readability Review</span>
                    <span>50.1s (2,244 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-[var(--color-surface-hover)] rounded-full overflow-hidden">
                    <div className="h-full w-[28%] bg-[var(--color-primary)] rounded-full" />
                  </div>
                </div>
                <div className="space-y-1">
                  <div className="flex justify-between text-[10px] font-semibold text-[var(--color-text-secondary)]">
                    <span>Keyword Matcher</span>
                    <span>99.1s (2,192 tokens)</span>
                  </div>
                  <div className="h-3 w-full bg-[var(--color-surface-hover)] rounded-full overflow-hidden">
                    <div className="h-full w-[55%] bg-[var(--color-primary)] rounded-full" />
                  </div>
                </div>
              </div>
              
              <div className="text-[10px] text-[var(--color-text-muted)] flex items-center space-x-1.5 justify-end">
                <AlertTriangle className="h-3 w-3" />
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
      {/* Clean application background wrapper */}
      <div className="w-screen h-screen overflow-hidden bg-[var(--color-background)]">
        <MainAppContent />
      </div>
    </CvProvider>
  );
}
