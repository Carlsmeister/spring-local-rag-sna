import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { 
  CheckCircle, 
  AlertTriangle, 
  Sparkles, 
  ArrowRight, 
  Briefcase, 
  FileText, 
  RefreshCw,
  Trophy,
  ShieldCheck,
  Cpu
} from 'lucide-react';
import { useCv } from '../context/CvContext';

interface AnalysisDashboardProps {
  setActiveTab: (tab: 'dashboard' | 'matcher' | 'rewriter' | 'admin') => void;
}

export default function AnalysisDashboard({ setActiveTab }: AnalysisDashboardProps) {
  const { analysisResult, activeFileName, clearActiveCv } = useCv();
  const [scoreCount, setScoreCount] = useState(0);

  if (!analysisResult) return null;

  const { atsScore, strengths, weaknesses, recommendations } = analysisResult;

  // Animated Count Up
  useEffect(() => {
    const duration = 800; // 0.8 seconds
    const end = atsScore;
    const startTime = performance.now();

    const animate = (timestamp: number) => {
      const elapsed = timestamp - startTime;
      const progress = Math.min(elapsed / duration, 1);
      
      // Easing out quadratic
      const easeProgress = progress * (2 - progress);
      const current = Math.floor(easeProgress * end);
      
      setScoreCount(current);

      if (progress < 1) {
        requestAnimationFrame(animate);
      } else {
        setScoreCount(end);
      }
    };

    requestAnimationFrame(animate);
  }, [atsScore]);

  // Color-shifting thresholds
  const getColorClasses = (score: number) => {
    if (score >= 80) return {
      stroke: 'stroke-[var(--color-success)]',
      text: 'text-[var(--color-success)]',
      bgBar: 'bg-[var(--color-success)]',
      bgGlow: 'bg-[var(--color-success-subtle)]',
      badge: 'bg-[var(--color-success-subtle)] text-[var(--color-success)] border border-[var(--color-success)]'
    };
    if (score >= 50) return {
      stroke: 'stroke-[var(--color-warning)]',
      text: 'text-[var(--color-warning)]',
      bgBar: 'bg-[var(--color-warning)]',
      bgGlow: 'bg-[var(--color-warning-subtle)]',
      badge: 'bg-[var(--color-warning-subtle)] text-[var(--color-warning)] border border-[var(--color-warning)]'
    };
    return {
      stroke: 'stroke-[var(--color-error)]',
      text: 'text-[var(--color-error)]',
      bgBar: 'bg-[var(--color-error)]',
      bgGlow: 'bg-[var(--color-error-subtle)]',
      badge: 'bg-[var(--color-error-subtle)] text-[var(--color-error)] border border-[var(--color-error)]'
    };
  };

  const colors = getColorClasses(atsScore);

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 10 },
    show: { opacity: 1, y: 0, transition: { duration: 0.3 } }
  };

  return (
    <motion.div 
      variants={containerVariants}
      initial="hidden"
      animate="show"
      className="space-y-6 w-full py-2 text-[var(--color-text-primary)]"
    >
      
      {/* 1. Header with file details and refresh trigger */}
      <motion.div 
        variants={itemVariants}
        className="flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0"
      >
        <div className="space-y-1">
          <div className="flex items-center space-x-2">
            <span className={`px-2.5 py-0.5 rounded text-[10px] font-bold tracking-wider uppercase ${colors.badge}`}>
              Analysis complete
            </span>
            <div className="flex items-center text-xs text-[var(--color-text-secondary)] bg-[var(--color-surface)] border border-[var(--color-border)] rounded px-2.5 py-1 shadow-sm">
              <FileText className="h-3.5 w-3.5 mr-1.5 text-[var(--color-primary)]" />
              <span className="font-semibold max-w-[200px] truncate">{activeFileName}</span>
            </div>
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-[var(--color-primary)]">
            CV Analysis Review
          </h2>
        </div>

        <button 
          onClick={clearActiveCv}
          className="flex items-center space-x-2 px-4 py-2 rounded-lg bg-white border border-[var(--color-border-strong)] text-[var(--color-primary)] font-semibold text-xs hover:bg-[var(--color-surface-hover)] transition-all duration-200 shadow-sm active:scale-95 flex-shrink-0"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          <span>Scan New Document</span>
        </button>
      </motion.div>

      {/* 2. Top Metric Row: Score & Diagnostic Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Score Progress Panel */}
        <motion.div 
          variants={itemVariants}
          className="lg:col-span-1 glass-panel rounded-xl p-8 flex flex-col justify-between min-h-[260px]"
        >
          <div className="space-y-4 w-full">
            <div className="flex justify-between items-center">
              <span className="text-xs font-bold uppercase tracking-wider text-[var(--color-text-secondary)]">ATS Compatibility</span>
              <span className={`text-3xl font-black ${colors.text}`}>{scoreCount}%</span>
            </div>
            
            {/* Horizontal Progress Bar */}
            <div className="h-3 w-full bg-[var(--color-surface-hover)] rounded-full overflow-hidden border border-[var(--color-border)]">
              <div 
                className={`h-full ${colors.bgBar} rounded-full transition-all duration-500`} 
                style={{ width: `${scoreCount}%` }} 
              />
            </div>
            
            <p className="text-[12px] text-[var(--color-text-secondary)] leading-relaxed pt-2">
              {atsScore >= 80 
                ? 'Strong keyword density and formatting layout. Good job!' 
                : atsScore >= 50 
                ? 'Moderate keyword alignment. Consider optimization suggestions.' 
                : 'Significant gaps discovered. Key improvements needed.'}
            </p>
          </div>
          
          <div className="border-t border-[var(--color-border)] pt-4 mt-4 w-full">
            <span className="text-[10px] text-[var(--color-text-muted)] font-semibold uppercase tracking-wider block">Assessment Metric</span>
            <span className="text-xs text-[var(--color-text-primary)] font-semibold mt-0.5 block flex items-center">
              <Trophy className="h-3.5 w-3.5 text-[var(--color-primary)] mr-1.5" />
              Document Structure Score
            </span>
          </div>
        </motion.div>

        {/* Quick Diagnostics Highlights Card */}
        <motion.div 
          variants={itemVariants}
          className="lg:col-span-2 glass-panel rounded-xl p-8 flex flex-col justify-between"
        >
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <Cpu className="h-4.5 w-4.5 text-[var(--color-primary)]" />
              <h4 className="font-bold text-xs tracking-wider uppercase text-[var(--color-primary)]">Audit Status</h4>
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
              <div className="p-4 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] flex items-center space-x-4">
                <div className="h-10 w-10 rounded bg-[var(--color-success-subtle)] border border-[var(--color-success)] flex items-center justify-center">
                  <CheckCircle className="h-5 w-5 text-[var(--color-success)]" />
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-[var(--color-text-secondary)] tracking-wider uppercase block">Strengths</span>
                  <span className="text-base font-bold text-[var(--color-text-primary)]">{strengths.length} Categories Passed</span>
                </div>
              </div>

              <div className="p-4 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] flex items-center space-x-4">
                <div className="h-10 w-10 rounded bg-[var(--color-warning-subtle)] border border-[var(--color-warning)] flex items-center justify-center">
                  <AlertTriangle className="h-5 w-5 text-[var(--color-warning)]" />
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-[var(--color-text-secondary)] tracking-wider uppercase block">Improvement opportunities</span>
                  <span className="text-base font-bold text-[var(--color-text-primary)]">{weaknesses.length} Structural Gaps</span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4 pt-6 border-t border-[var(--color-border)]">
            <div className="flex items-center text-[10px] text-[var(--color-text-muted)] font-semibold uppercase tracking-wide space-x-1.5">
              <ShieldCheck className="h-4 w-4 text-[var(--color-success)]" />
              <span>Factual guardrails active</span>
            </div>
            <span className="hidden sm:inline text-[var(--color-border-strong)]">|</span>
            <div className="flex items-center text-[10px] text-[var(--color-text-muted)] font-semibold uppercase tracking-wide space-x-1.5">
              <Cpu className="h-4 w-4 text-[var(--color-primary)]" />
              <span>Local processing active</span>
            </div>
          </div>
        </motion.div>

      </div>

      {/* 3. Deep Analysis Review Grid */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        
        {/* Left Side: Passed Strengths List */}
        <motion.div 
          variants={itemVariants}
          className="glass-panel rounded-xl p-8 flex flex-col space-y-6"
        >
          <div className="flex items-center space-x-3">
            <div className="h-8 w-8 rounded bg-[var(--color-success-subtle)] border border-[var(--color-success)] flex items-center justify-center">
              <CheckCircle className="h-4 w-4 text-[var(--color-success)]" />
            </div>
            <div>
              <h3 className="font-bold text-base text-[var(--color-text-primary)]">Key Document Strengths</h3>
              <p className="text-[11px] text-[var(--color-text-secondary)]">Identified competitive formatting assets.</p>
            </div>
          </div>

          <div className="space-y-3">
            {strengths.map((item, idx) => (
              <motion.div 
                key={idx}
                className="flex items-start space-x-3 p-3.5 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] hover:bg-[var(--color-surface-hover)] transition-colors duration-200"
              >
                <CheckCircle className="h-4 w-4 text-[var(--color-success)] flex-shrink-0 mt-0.5" />
                <span className="text-xs text-[var(--color-text-primary)] leading-relaxed font-medium">{item}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Right Side: Discovered Improvement Opportunities */}
        <motion.div 
          variants={itemVariants}
          className="glass-panel rounded-xl p-8 flex flex-col space-y-6"
        >
          <div className="flex items-center space-x-3">
            <div className="h-8 w-8 rounded bg-[var(--color-warning-subtle)] border border-[var(--color-warning)] flex items-center justify-center">
              <AlertTriangle className="h-4 w-4 text-[var(--color-warning)]" />
            </div>
            <div>
              <h3 className="font-bold text-base text-[var(--color-text-primary)]">Improvement opportunities</h3>
              <p className="text-[11px] text-[var(--color-text-secondary)]">Recommended adjustments to improve compliance.</p>
            </div>
          </div>

          <div className="space-y-3">
            {weaknesses.map((item, idx) => (
              <motion.div 
                key={idx}
                className="flex items-start space-x-3 p-3.5 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] hover:bg-[var(--color-surface-hover)] transition-colors duration-200"
              >
                <AlertTriangle className="h-4 w-4 text-[var(--color-warning)] flex-shrink-0 mt-0.5" />
                <span className="text-xs text-[var(--color-text-primary)] leading-relaxed font-medium">{item}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

      </div>

      {/* 4. Actionable Optimization Recommendations */}
      <motion.div 
        variants={itemVariants}
        className="glass-panel rounded-xl p-8 flex flex-col space-y-6"
      >
        <div className="flex items-center space-x-3">
          <div className="h-8 w-8 rounded bg-[var(--color-primary-subtle)] border border-[var(--color-primary)] flex items-center justify-center">
            <Sparkles className="h-4 w-4 text-[var(--color-primary)]" />
          </div>
          <div>
            <h3 className="font-bold text-base text-[var(--color-text-primary)]">Step-by-Step Optimization Guide</h3>
            <p className="text-[11px] text-[var(--color-text-secondary)]">Actionable strategy compiled by local AI review agents.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {recommendations.map((item, idx) => (
            <motion.div 
              key={idx}
              className="flex items-start space-x-3.5 p-4 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] hover:border-[var(--color-border-strong)] hover:bg-[var(--color-surface-hover)] transition-all duration-200"
            >
              <div className="h-5 w-5 rounded bg-[var(--color-primary-subtle)] border border-[var(--color-border)] flex items-center justify-center text-[10px] font-bold text-[var(--color-primary)] flex-shrink-0 mt-0.5">
                {idx + 1}
              </div>
              <span className="text-xs text-[var(--color-text-primary)] leading-relaxed font-medium">{item}</span>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* 5. Bottom Navigation / Redirect Guides */}
      <motion.div 
        variants={itemVariants}
        className="grid grid-cols-1 md:grid-cols-2 gap-6"
      >
        <div className="glass-panel rounded-xl p-6 flex flex-col justify-between group hover:border-[var(--color-border-strong)] transition-all duration-200">
          <div>
            <div className="h-9 w-9 rounded bg-[var(--color-primary-subtle)] border border-[var(--color-border)] flex items-center justify-center mb-4 text-[var(--color-primary)] group-hover:scale-105 transition-transform duration-200">
              <Briefcase className="h-4.5 w-4.5" />
            </div>
            <h4 className="font-bold text-sm text-[var(--color-text-primary)]">Job Matcher Workspace</h4>
            <p className="text-[var(--color-text-secondary)] text-xs mt-2 leading-relaxed">
              Verify if your resume has keyword density compliance. Paste specific vacancy criteria to highlight compatibility gaps semantically.
            </p>
          </div>
          <button 
            onClick={() => setActiveTab('matcher')}
            className="mt-6 flex items-center space-x-1.5 text-xs font-bold text-[var(--color-primary)] hover:text-[var(--color-primary-hover)] self-start"
          >
            <span>Proceed to Matcher Workspace</span>
            <ArrowRight className="h-3.5 w-3.5 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>

        <div className="glass-panel rounded-xl p-6 flex flex-col justify-between group hover:border-[var(--color-border-strong)] transition-all duration-200">
          <div>
            <div className="h-9 w-9 rounded bg-[var(--color-primary-subtle)] border border-[var(--color-border)] flex items-center justify-center mb-4 text-[var(--color-primary)] group-hover:scale-105 transition-transform duration-200">
              <Sparkles className="h-4.5 w-4.5" />
            </div>
            <h4 className="font-bold text-sm text-[var(--color-text-primary)]">Bullet Point Optimizer</h4>
            <p className="text-[var(--color-text-secondary)] text-xs mt-2 leading-relaxed">
              Restructure individual CV bullet points. Choose styling configurations and generate rewrites strictly guarded against fabrication.
            </p>
          </div>
          <button 
            onClick={() => setActiveTab('rewriter')}
            className="mt-6 flex items-center space-x-1.5 text-xs font-bold text-[var(--color-primary)] hover:text-[var(--color-primary-hover)] self-start"
          >
            <span>Proceed to Optimizer Workspace</span>
            <ArrowRight className="h-3.5 w-3.5 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>

      </motion.div>

    </motion.div>
  );
}
