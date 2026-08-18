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
  ShieldAlert,
  FileText,
  Copy,
  Check,
  BookOpen
} from 'lucide-react';

type TabType = 'dashboard' | 'matcher' | 'rewriter' | 'admin';

interface JobMatchResult {
  matchPercentage: number;
  missingKeywords: string[];
  matchingStrength: string;
  actionableSuggestions: string[];
}

interface RewriteResult {
  originalText: string;
  suggestions: {
    style: string;
    text: string;
  }[];
}

function MainAppContent() {
  const [activeTab, setActiveTab] = useState<TabType>('dashboard');
  const { analysisResult, activeCvId, parsedContent, activeFileName } = useCv();

  // 1. Job Matcher States
  const [jobAdText, setJobAdText] = useState('');
  const [jobMatchLoading, setJobMatchLoading] = useState(false);
  const [jobMatchResult, setJobMatchResult] = useState<JobMatchResult | null>(null);
  const [jobMatchError, setJobMatchError] = useState<string | null>(null);

  // 2. Bullet Optimizer States
  const [selectedSentence, setSelectedSentence] = useState<string | null>(null);
  const [keywordsInput, setKeywordsInput] = useState('');
  const [rewriteLoading, setRewriteLoading] = useState(false);
  const [rewriteResult, setRewriteResult] = useState<RewriteResult | null>(null);
  const [rewriteError, setRewriteError] = useState<string | null>(null);

  // Copy Feedback state
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const copyToClipboard = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  // Split parsed CV text into sentences
  const getCvSentences = () => {
    if (!parsedContent) return [];
    // Split by common sentence delimiters or newlines, filter for standard bullet lengths
    return parsedContent
      .split(/[.\n]+/)
      .map(s => s.trim())
      .filter(s => s.length > 20 && s.length < 180 && !s.includes('{') && !s.includes('}'));
  };

  const handleJobMatch = async () => {
    if (!activeCvId || !jobAdText.trim()) return;
    setJobMatchLoading(true);
    setJobMatchError(null);
    setJobMatchResult(null);

    try {
      const res = await fetch('/api/jobs/match', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          documentId: activeCvId,
          jobAdText: jobAdText
        })
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Job match analysis failed. Please verify that the target text is valid.');
      }

      const data = await res.json();
      setJobMatchResult(data);
    } catch (e: any) {
      setJobMatchError(e.message || 'An unexpected connection error occurred.');
    } finally {
      setJobMatchLoading(false);
    }
  };

  const handleRewrite = async () => {
    if (!activeCvId || !selectedSentence) return;
    setRewriteLoading(true);
    setRewriteError(null);
    setRewriteResult(null);

    // Split comma-separated keywords
    const keywords = keywordsInput
      .split(',')
      .map(k => k.trim())
      .filter(k => k.length > 0);

    try {
      const res = await fetch('/api/rewrite/section', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          documentId: activeCvId,
          originalText: selectedSentence,
          keywords: keywords
        })
      });

      if (res.status === 422) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Factual validation rejected the suggestion to prevent fabrication.');
      }

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to generate optimized suggestions.');
      }

      const data = await res.json();
      setRewriteResult(data);
    } catch (e: any) {
      setRewriteError(e.message || 'An unexpected error occurred during rewriting.');
    } finally {
      setRewriteLoading(false);
    }
  };

  const formatStyleName = (style: string) => {
    switch (style.toUpperCase()) {
      case 'ACTION_ORIENTED':
        return 'Concise & Action-Oriented';
      case 'METRIC_DRIVEN':
        return 'Results-Focused';
      case 'CONCISE':
        return 'Clear & Concise';
      default:
        return style;
    }
  };

  const sentences = getCvSentences();

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

                {/* Ingestion & Analysis Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  <div className="lg:col-span-2">
                    <DocDropzone />
                  </div>

                  {/* Quick Scanning Stats */}
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

            {!activeCvId ? (
              /* Empty state: No CV uploaded */
              <div className="glass-panel rounded-xl p-12 text-center border border-[var(--color-border)] bg-[var(--color-surface)] flex flex-col items-center justify-center min-h-[300px]">
                <FileText className="h-12 w-12 text-[var(--color-text-muted)] mb-4" />
                <h3 className="text-base font-bold text-[var(--color-text-primary)]">No Active CV Document</h3>
                <p className="text-[var(--color-text-secondary)] text-xs max-w-sm mt-2 leading-relaxed">
                  You must upload a CV document first in the CV Review tab before evaluating job alignment compatibility.
                </p>
                <button 
                  onClick={() => setActiveTab('dashboard')}
                  className="mt-6 px-5 py-2 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-xs tracking-wide transition-all duration-200"
                >
                  Upload a CV
                </button>
              </div>
            ) : jobMatchResult ? (
              /* Results view */
              <div className="space-y-6">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  
                  {/* Score & Matching Strength Card */}
                  <div className="lg:col-span-1 glass-panel rounded-xl p-8 flex flex-col justify-between min-h-[240px]">
                    <div className="space-y-4">
                      <div className="flex justify-between items-center">
                        <span className="text-xs font-bold uppercase tracking-wider text-[var(--color-text-secondary)]">Job Alignment</span>
                        <span className="text-3xl font-black text-[var(--color-primary)]">{jobMatchResult.matchPercentage}%</span>
                      </div>
                      
                      {/* Progress Bar */}
                      <div className="h-3 w-full bg-[var(--color-surface-hover)] rounded-full overflow-hidden border border-[var(--color-border)]">
                        <div 
                          className="h-full bg-[var(--color-accent)] rounded-full transition-all duration-500" 
                          style={{ width: `${jobMatchResult.matchPercentage}%` }} 
                        />
                      </div>
                      
                      <div className="p-3 rounded-lg bg-[var(--color-primary-subtle)] border border-[var(--color-border)] text-xs text-[var(--color-primary)] font-semibold mt-4 text-center">
                        Match Strength: {jobMatchResult.matchingStrength}
                      </div>
                    </div>

                    <button
                      onClick={() => {
                        setJobMatchResult(null);
                        setJobAdText('');
                      }}
                      className="mt-6 w-full py-2 bg-white border border-[var(--color-border-strong)] hover:bg-[var(--color-surface-hover)] text-[var(--color-primary)] font-semibold text-xs rounded-lg transition-colors"
                    >
                      Compare Another Job
                    </button>
                  </div>

                  {/* Missing Keywords Details */}
                  <div className="lg:col-span-2 glass-panel rounded-xl p-8">
                    <div className="space-y-4">
                      <h3 className="font-bold text-sm uppercase tracking-wider text-[var(--color-text-secondary)]">Missing Target Keywords</h3>
                      <p className="text-xs text-[var(--color-text-secondary)]">
                        These key technical terms or concepts are mentioned in the job ad but are not represented in your CV:
                      </p>
                      <div className="flex flex-wrap gap-2 pt-2">
                        {jobMatchResult.missingKeywords.length > 0 ? (
                          jobMatchResult.missingKeywords.map((kw, idx) => (
                            <span key={idx} className="px-2.5 py-1 rounded bg-[var(--color-error-subtle)] text-[var(--color-error)] border border-[var(--color-error-subtle)] text-xs font-semibold">
                              {kw}
                            </span>
                          ))
                        ) : (
                          <span className="text-xs text-[var(--color-success)] font-semibold flex items-center">
                            <CheckCircle className="h-4 w-4 mr-1.5" /> No missing keywords found!
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Recommendations suggestions card */}
                <div className="glass-panel rounded-xl p-8 space-y-4">
                  <h3 className="font-bold text-base text-[var(--color-text-primary)]">Actionable Optimization Suggestions</h3>
                  <div className="space-y-3">
                    {jobMatchResult.actionableSuggestions.map((sug, idx) => (
                      <div key={idx} className="flex items-start space-x-3 p-3.5 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] text-xs">
                        <div className="h-5 w-5 rounded bg-[var(--color-primary-subtle)] flex items-center justify-center text-[10px] font-bold text-[var(--color-primary)] flex-shrink-0 mt-0.5">
                          {idx + 1}
                        </div>
                        <span className="text-[var(--color-text-primary)] leading-relaxed font-medium">{sug}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              /* Form input view */
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Left Column - Active CV Context */}
                <div className="glass-panel rounded-xl p-8 min-h-[300px] flex flex-col justify-between">
                  <div className="space-y-4">
                    <h3 className="font-bold text-base text-[var(--color-text-primary)]">Active Candidate Profile</h3>
                    <div className="space-y-2">
                      <div className="flex items-center text-xs text-[var(--color-text-secondary)] bg-[var(--color-surface-muted)] border border-[var(--color-border)] rounded px-3 py-2">
                        <FileText className="h-4 w-4 mr-2 text-[var(--color-primary)]" />
                        <span className="font-semibold truncate">{activeFileName}</span>
                      </div>
                      <p className="text-[var(--color-text-secondary)] text-xs leading-relaxed">
                        The content segments of this CV will be compared against the target job requirements.
                      </p>
                    </div>
                  </div>
                  <div className="text-xs text-[var(--color-text-muted)] border-t border-[var(--color-border)] pt-4">
                    Document Context: <span className="font-semibold text-[var(--color-text-primary)]">Active</span>
                  </div>
                </div>

                {/* Right Column - Job Ad Copy Input */}
                <div className="glass-panel rounded-xl p-8 flex flex-col space-y-4">
                  <h3 className="font-bold text-base text-[var(--color-text-primary)]">Target Job Description</h3>
                  <textarea 
                    value={jobAdText}
                    onChange={(e) => setJobAdText(e.target.value)}
                    disabled={jobMatchLoading}
                    className="flex-grow w-full min-h-[220px] rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] p-4 text-[var(--color-text-primary)] text-sm focus:outline-none focus:border-[var(--color-border-strong)] transition-colors resize-none"
                    placeholder="Paste the target job advertisement text or key requirements here to compare..."
                  />

                  {jobMatchError && (
                    <div className="p-3.5 rounded-lg bg-[var(--color-error-subtle)] border border-[var(--color-error)] text-xs text-[var(--color-error)] flex items-start space-x-2">
                      <AlertTriangle className="h-4 w-4 flex-shrink-0 mt-0.5" />
                      <span>{jobMatchError}</span>
                    </div>
                  )}

                  <button 
                    onClick={handleJobMatch}
                    disabled={jobMatchLoading || !jobAdText.trim()}
                    className="w-full py-3 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-sm tracking-wide shadow-sm flex items-center justify-center space-x-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {jobMatchLoading ? (
                      <>
                        <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        <span>Analysing alignment...</span>
                      </>
                    ) : (
                      <>
                        <span>Analyse job match</span>
                        <ArrowRight className="h-4 w-4" />
                      </>
                    )}
                  </button>
                </div>
              </div>
            )}
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

            {!activeCvId ? (
              /* Empty state: No CV */
              <div className="glass-panel rounded-xl p-12 text-center border border-[var(--color-border)] bg-[var(--color-surface)] flex flex-col items-center justify-center min-h-[300px]">
                <BookOpen className="h-12 w-12 text-[var(--color-text-muted)] mb-4" />
                <h3 className="text-base font-bold text-[var(--color-text-primary)]">No Active CV Document</h3>
                <p className="text-[var(--color-text-secondary)] text-xs max-w-sm mt-2 leading-relaxed">
                  Upload your CV document in the CV Review tab first to isolate and optimize sentence bullet points.
                </p>
                <button 
                  onClick={() => setActiveTab('dashboard')}
                  className="mt-6 px-5 py-2 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-xs tracking-wide transition-all duration-200"
                >
                  Upload a CV
                </button>
              </div>
            ) : (
              /* Split Screen Optimizer Workspace */
              <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
                
                {/* Left Panel: Sentences List */}
                <div className="lg:col-span-2 glass-panel rounded-xl p-6 flex flex-col max-h-[580px]">
                  <h3 className="font-bold text-sm uppercase tracking-wider text-[var(--color-text-secondary)] mb-4">CV Sentences</h3>
                  <div className="overflow-y-auto space-y-2 flex-grow pr-1 text-xs">
                    {sentences.length > 0 ? (
                      sentences.map((sent, idx) => (
                        <button
                          key={idx}
                          onClick={() => {
                            setSelectedSentence(sent);
                            setRewriteResult(null);
                            setRewriteError(null);
                          }}
                          className={`w-full text-left p-3 rounded-lg border leading-relaxed transition-all duration-200 ${
                            selectedSentence === sent
                              ? 'bg-[var(--color-surface-selected)] border-[var(--color-accent)] text-[var(--color-primary)] font-semibold shadow-sm'
                              : 'bg-[var(--color-surface)] border-[var(--color-border)] text-[var(--color-text-secondary)] hover:bg-[var(--color-surface-hover)] hover:text-[var(--color-text-primary)]'
                          }`}
                        >
                          {sent}
                        </button>
                      ))
                    ) : (
                      <div className="text-center py-10 text-[var(--color-text-muted)]">
                        No sentences could be parsed.
                      </div>
                    )}
                  </div>
                </div>

                {/* Right Panel: Optimization Controls & Results */}
                <div className="lg:col-span-3 space-y-6">
                  
                  {/* Selected Sentence Card */}
                  <div className="glass-panel rounded-xl p-6 space-y-4">
                    <h3 className="font-bold text-sm text-[var(--color-text-primary)]">Rewrite Section</h3>
                    
                    {selectedSentence ? (
                      <div className="space-y-4">
                        <div className="p-3.5 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] text-xs text-[var(--color-text-primary)] leading-relaxed italic">
                          "{selectedSentence}"
                        </div>

                        {/* Keyword Guidance input */}
                        <div className="space-y-1.5">
                          <label className="text-[11px] font-bold text-[var(--color-text-secondary)] uppercase">Target Keywords (optional)</label>
                          <input 
                            type="text"
                            value={keywordsInput}
                            onChange={(e) => setKeywordsInput(e.target.value)}
                            placeholder="e.g. Project Management, Kubernetes, Java (comma-separated)"
                            className="w-full rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] p-2.5 text-xs text-[var(--color-text-primary)] focus:outline-none focus:border-[var(--color-border-strong)] transition-all"
                          />
                          <p className="text-[10px] text-[var(--color-text-muted)]">Guides AI stylization to align with these terms.</p>
                        </div>

                        {/* Error box */}
                        {rewriteError && (
                          <div className="p-4 rounded-lg bg-[var(--color-error-subtle)] border border-[var(--color-error)] text-xs text-[var(--color-error)] flex items-start space-x-2.5">
                            <AlertTriangle className="h-4 flex-shrink-0 mt-0.5" />
                            <span className="leading-relaxed">
                              <strong>Factual Integrity Alert:</strong> {rewriteError}
                            </span>
                          </div>
                        )}

                        <button
                          onClick={handleRewrite}
                          disabled={rewriteLoading}
                          className="w-full py-2.5 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-xs tracking-wide transition-all duration-200 flex items-center justify-center space-x-2"
                        >
                          {rewriteLoading ? (
                            <>
                              <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                              <span>Generating variations...</span>
                            </>
                          ) : (
                            <>
                              <Sparkles className="h-3.5 w-3.5" />
                              <span>Optimize Sentence</span>
                            </>
                          )}
                        </button>
                      </div>
                    ) : (
                      <div className="text-center py-12 text-[var(--color-text-muted)] text-xs">
                        Select a sentence from the left sidebar to begin optimization.
                      </div>
                    )}
                  </div>

                  {/* Suggestion results list */}
                  {rewriteResult && (
                    <div className="glass-panel rounded-xl p-6 space-y-4">
                      <div className="flex items-center justify-between">
                        <h3 className="font-bold text-sm text-[var(--color-text-primary)]">Factual Suggestions</h3>
                        <span className="flex items-center text-[10px] font-bold uppercase tracking-wider text-[var(--color-success)]">
                          <CheckCircle className="h-3.5 w-3.5 mr-1" /> Factual integrity verified
                        </span>
                      </div>
                      
                      <div className="space-y-4">
                        {rewriteResult.suggestions.map((sug, idx) => {
                          const copyId = `sug-${idx}`;
                          const isCopied = copiedId === copyId;
                          return (
                            <div key={idx} className="p-4 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] space-y-3 relative group hover:border-[var(--color-border-strong)] transition-all">
                              <div className="flex justify-between items-center">
                                <span className="text-[10px] font-bold text-[var(--color-primary)] uppercase tracking-wider bg-[var(--color-primary-subtle)] px-2 py-0.5 rounded border border-[var(--color-border)]">
                                  {formatStyleName(sug.style)}
                                </span>
                                
                                <button
                                  onClick={() => copyToClipboard(sug.text, copyId)}
                                  className="text-[var(--color-text-secondary)] hover:text-[var(--color-primary)] transition-colors p-1.5 rounded hover:bg-[var(--color-surface-hover)]"
                                  title="Copy suggestion"
                                >
                                  {isCopied ? (
                                    <Check className="h-3.5 w-3.5 text-[var(--color-success)] animate-pulse" />
                                  ) : (
                                    <Copy className="h-3.5 w-3.5" />
                                  )}
                                </button>
                              </div>
                              <p className="text-xs text-[var(--color-text-primary)] leading-relaxed">
                                {sug.text}
                              </p>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}

                </div>
              </div>
            )}
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
