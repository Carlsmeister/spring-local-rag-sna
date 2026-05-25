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

  // Circular SVG configurations
  const radius = 60;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (scoreCount / 100) * circumference;

  // Animated Count Up
  useEffect(() => {
    const duration = 1200; // 1.2 seconds
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
      stroke: 'stroke-emerald-500',
      text: 'text-emerald-400',
      glow: 'shadow-emerald-500/20 border-emerald-500/20',
      bgGlow: 'bg-emerald-500/10 border-emerald-500/15',
      badge: 'bg-emerald-950/30 text-emerald-400 border border-emerald-800/40'
    };
    if (score >= 50) return {
      stroke: 'stroke-amber-500',
      text: 'text-amber-400',
      glow: 'shadow-amber-500/20 border-amber-500/20',
      bgGlow: 'bg-amber-500/10 border-amber-500/15',
      badge: 'bg-amber-950/30 text-amber-400 border border-amber-800/40'
    };
    return {
      stroke: 'stroke-rose-500',
      text: 'text-rose-400',
      glow: 'shadow-rose-500/20 border-rose-500/20',
      bgGlow: 'bg-rose-500/10 border-rose-500/15',
      badge: 'bg-rose-950/30 text-rose-400 border border-rose-800/40'
    };
  };

  const colors = getColorClasses(atsScore);

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.15
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0, transition: { type: 'spring' as const, stiffness: 100 } }
  };

  return (
    <motion.div 
      variants={containerVariants}
      initial="hidden"
      animate="show"
      className="space-y-8 animate-fadeIn w-full py-2"
    >
      
      {/* 1. Header with file details and refresh trigger */}
      <motion.div 
        variants={itemVariants}
        className="flex flex-col sm:flex-row justify-between items-start sm:items-center space-y-4 sm:space-y-0"
      >
        <div>
          <div className="flex items-center space-x-2.5">
            <span className={`px-2.5 py-0.5 rounded-md text-[10px] font-bold tracking-wider uppercase ${colors.badge}`}>
              Analysis complete
            </span>
            <div className="flex items-center text-xs text-slate-400 bg-slate-900/40 border border-slate-800/60 rounded-lg px-2.5 py-1">
              <FileText className="h-3.5 w-3.5 mr-1.5 text-indigo-400" />
              <span className="font-semibold max-w-[200px] truncate">{activeFileName}</span>
            </div>
          </div>
          <h2 className="text-3xl font-extrabold tracking-tight mt-2.5">
            AI Document <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-400 to-purple-400">Feedback Dashboard</span>
          </h2>
        </div>

        <button 
          onClick={clearActiveCv}
          className="flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-slate-900/60 border border-slate-800/60 text-slate-300 font-semibold text-sm hover:border-indigo-500/40 hover:text-slate-100 transition-all duration-200 shadow-md active:scale-95 flex-shrink-0"
        >
          <RefreshCw className="h-4 w-4" />
          <span>Scan New Document</span>
        </button>
      </motion.div>

      {/* 2. Top Metric Row: Score & Strengths/Weaknesses counts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Neon Score Gauge Panel */}
        <motion.div 
          variants={itemVariants}
          className="lg:col-span-1 glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col items-center justify-center relative min-h-[300px] shadow-2xl"
        >
          {/* Internal Pulse Shadow Glow */}
          <div className={`absolute inset-0 rounded-3xl bg-transparent border-2 border-transparent transition-shadow duration-500 shadow-inner ${colors.glow}`} />
          
          <div className="relative flex items-center justify-center">
            {/* SVG Circular Track and Flowing Stroke */}
            <svg className="h-36 w-36 transform -rotate-90">
              <circle
                cx="72"
                cy="72"
                r={radius}
                className="stroke-slate-800/40 fill-transparent"
                strokeWidth="10"
              />
              <motion.circle
                cx="72"
                cy="72"
                r={radius}
                className={`${colors.stroke} fill-transparent transition-all duration-300`}
                strokeWidth="10"
                strokeDasharray={circumference}
                strokeDashoffset={strokeDashoffset}
                strokeLinecap="round"
              />
            </svg>

            {/* Score Text Counter inside */}
            <div className="absolute flex flex-col items-center text-center">
              <span className={`text-4xl font-black tracking-tight ${colors.text}`}>
                {scoreCount}%
              </span>
              <span className="text-[10px] font-bold tracking-wider uppercase text-slate-500 mt-1">
                ATS Score
              </span>
            </div>
          </div>

          <div className="mt-6 text-center space-y-1">
            <h4 className="font-extrabold text-slate-200 flex items-center justify-center space-x-1.5">
              <Trophy className="h-4 w-4 text-indigo-400" />
              <span>ATS Compatibility Index</span>
            </h4>
            <p className="text-slate-400 text-xs px-4">
              {atsScore >= 80 
                ? 'Strong keyword density and structure. Web-ready compliance.' 
                : atsScore >= 50 
                ? 'Moderate keyword alignment. Bullet restructuring advised.' 
                : 'Significant gaps discovered. Prompt optimizations required.'}
            </p>
          </div>
        </motion.div>

        {/* Quick Diagnostics Highlights Card */}
        <motion.div 
          variants={itemVariants}
          className="lg:col-span-2 glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col justify-between"
        >
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <Cpu className="h-5 w-5 text-indigo-400" />
              <h4 className="font-bold text-sm tracking-wider uppercase text-indigo-400">Audit Status</h4>
            </div>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mt-4">
              <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/60 flex items-center space-x-4">
                <div className="h-10 w-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
                  <CheckCircle className="h-5 w-5 text-emerald-400" />
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-slate-500 tracking-wider uppercase block">Strengths</span>
                  <span className="text-lg font-black text-slate-200">{strengths.length} Categories Passed</span>
                </div>
              </div>

              <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/60 flex items-center space-x-4">
                <div className="h-10 w-10 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
                  <AlertTriangle className="h-5 w-5 text-amber-400" />
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-slate-500 tracking-wider uppercase block">Warnings</span>
                  <span className="text-lg font-black text-slate-200">{weaknesses.length} Structural Gaps</span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4 pt-6 border-t border-slate-900">
            <div className="flex items-center text-[11px] text-slate-500 space-x-2">
              <ShieldCheck className="h-4 w-4 text-emerald-500/60" />
              <span>Factual guardrails verified successfully</span>
            </div>
            <span className="hidden sm:inline text-slate-700">|</span>
            <div className="flex items-center text-[11px] text-slate-500 space-x-2">
              <Cpu className="h-4 w-4 text-indigo-500/60" />
              <span>Inference: Local Ollama Model (gemma4:2eb)</span>
            </div>
          </div>
        </motion.div>

      </div>

      {/* 3. Deep Analysis Review Grid */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
        
        {/* Left Side: Passed Strengths List */}
        <motion.div 
          variants={itemVariants}
          className="glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col space-y-6"
        >
          <div className="flex items-center space-x-3">
            <div className="h-9 w-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
              <CheckCircle className="h-5 w-5 text-emerald-400" />
            </div>
            <div>
              <h3 className="font-extrabold text-lg text-slate-100">Key Document Strengths</h3>
              <p className="text-[11px] text-slate-400 -mt-0.5">Identified competitive formatting assets.</p>
            </div>
          </div>

          <div className="space-y-3">
            {strengths.map((item, idx) => (
              <motion.div 
                key={idx}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 * idx }}
                className="flex items-start space-x-3 p-3.5 rounded-2xl bg-emerald-950/5 border border-emerald-900/10 hover:border-emerald-500/10 transition-colors"
              >
                <CheckCircle className="h-5 w-5 text-emerald-400/80 flex-shrink-0 mt-0.5" />
                <span className="text-sm text-slate-300 leading-relaxed font-medium">{item}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Right Side: Discovered Weaknesses List */}
        <motion.div 
          variants={itemVariants}
          className="glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col space-y-6"
        >
          <div className="flex items-center space-x-3">
            <div className="h-9 w-9 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
              <AlertTriangle className="h-5 w-5 text-amber-400" />
            </div>
            <div>
              <h3 className="font-extrabold text-lg text-slate-100">Structural Gaps & Weaknesses</h3>
              <p className="text-[11px] text-slate-400 -mt-0.5">Recommended adjustments to improve compliance.</p>
            </div>
          </div>

          <div className="space-y-3">
            {weaknesses.map((item, idx) => (
              <motion.div 
                key={idx}
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.1 * idx }}
                className="flex items-start space-x-3 p-3.5 rounded-2xl bg-amber-950/5 border border-amber-900/10 hover:border-amber-500/10 transition-colors"
              >
                <AlertTriangle className="h-5 w-5 text-amber-400/80 flex-shrink-0 mt-0.5" />
                <span className="text-sm text-slate-300 leading-relaxed font-medium">{item}</span>
              </motion.div>
            ))}
          </div>
        </motion.div>

      </div>

      {/* 4. Actionable Optimization Recommendations */}
      <motion.div 
        variants={itemVariants}
        className="glass-panel rounded-3xl p-8 border border-slate-800/40 flex flex-col space-y-6"
      >
        <div className="flex items-center space-x-3">
          <div className="h-9 w-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
            <Sparkles className="h-5 w-5 text-indigo-400" />
          </div>
          <div>
            <h3 className="font-extrabold text-lg text-slate-100">Step-by-Step Optimization Guide</h3>
            <p className="text-[11px] text-slate-400 -mt-0.5">Actionable strategy compiled by local AI review agents.</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {recommendations.map((item, idx) => (
            <motion.div 
              key={idx}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.05 * idx }}
              className="flex items-start space-x-3.5 p-4 rounded-2xl bg-slate-900/30 border border-slate-800/40 hover:border-indigo-500/20 transition-all duration-300"
            >
              <div className="h-6 w-6 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-[10px] font-bold text-indigo-400 flex-shrink-0 mt-0.5">
                {idx + 1}
              </div>
              <span className="text-sm text-slate-300 leading-relaxed font-medium">{item}</span>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* 5. Bottom Navigation / Redirect Guides */}
      <motion.div 
        variants={itemVariants}
        className="glass-panel rounded-3xl p-8 border border-slate-800/40 grid grid-cols-1 md:grid-cols-2 gap-6 relative overflow-hidden"
      >
        {/* Subtle decorative lights */}
        <div className="absolute right-0 bottom-0 h-40 w-40 rounded-full bg-indigo-500/[0.02] blur-3xl" />
        <div className="absolute left-0 top-0 h-40 w-40 rounded-full bg-purple-500/[0.02] blur-3xl" />

        <div className="p-6 rounded-2xl bg-slate-900/20 border border-slate-800/40 flex flex-col justify-between relative group hover:border-indigo-500/20 transition-all duration-300">
          <div>
            <div className="h-10 w-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mb-4 group-hover:scale-105 transition-transform duration-300">
              <Briefcase className="h-5 w-5 text-indigo-400" />
            </div>
            <h4 className="font-extrabold text-slate-200">Job Advertisement Matcher</h4>
            <p className="text-slate-400 text-xs mt-2 leading-relaxed">
              Verify if your resume has structural keyword density compliance. Paste specific vacancy criteria to highlight compatibility gaps semantically.
            </p>
          </div>
          <button 
            onClick={() => setActiveTab('matcher')}
            className="mt-6 flex items-center space-x-2 text-xs font-bold text-indigo-400 hover:text-indigo-300 group-hover:translate-x-1 transition-all"
          >
            <span>Proceed to Matcher Workspace</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </div>

        <div className="p-6 rounded-2xl bg-slate-900/20 border border-slate-800/40 flex flex-col justify-between relative group hover:border-indigo-500/20 transition-all duration-300">
          <div>
            <div className="h-10 w-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mb-4 group-hover:scale-105 transition-transform duration-300">
              <Sparkles className="h-5 w-5 text-indigo-400" />
            </div>
            <h4 className="font-extrabold text-slate-200">Interactive Sentence Optimizer</h4>
            <p className="text-slate-400 text-xs mt-2 leading-relaxed">
              Restructure individual CV bullet points. Choose styling configurations (Concise, Metrics-driven) and generate rewrites guarded against fabrication.
            </p>
          </div>
          <button 
            onClick={() => setActiveTab('rewriter')}
            className="mt-6 flex items-center space-x-2 text-xs font-bold text-indigo-400 hover:text-indigo-300 group-hover:translate-x-1 transition-all"
          >
            <span>Proceed to Optimizer Workspace</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </div>

      </motion.div>

    </motion.div>
  );
}
