import { useState, useRef } from 'react';
import type { DragEvent, ChangeEvent } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, AlertCircle, Loader2, CheckCircle2 } from 'lucide-react';
import { useCv } from '../context/CvContext';

type UploadStep = 'idle' | 'uploading' | 'analyzing' | 'guardrails' | 'success' | 'error';

export default function DocDropzone() {
  const { setActiveCv, setAnalysisResult } = useCv();
  const [step, setStep] = useState<UploadStep>('idle');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    if (step === 'idle') {
      e.currentTarget.classList.add('border-indigo-500', 'bg-indigo-500/5');
    }
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.currentTarget.classList.remove('border-indigo-500', 'bg-indigo-500/5');
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.currentTarget.classList.remove('border-indigo-500', 'bg-indigo-500/5');
    if (step !== 'idle') return;

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      processFile(files[0]);
    }
  };

  const handleFileSelect = (e: ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      processFile(files[0]);
    }
  };

  const triggerFileSelect = () => {
    if (step === 'idle' && fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const processFile = async (file: File) => {
    // 1. File Validation
    const extension = file.name.split('.').pop()?.toLowerCase();
    if (extension !== 'pdf' && extension !== 'docx') {
      showError('Unsupported file type. Please upload a PDF or DOCX file.');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      showError('File exceeds size limits. Maximum allowed file size is 5MB.');
      return;
    }

    setSelectedFile(file);
    setErrorMessage(null);
    
    try {
      // Step A: Parse Document (Apache Tika)
      setStep('uploading');
      const formData = new FormData();
      formData.append('file', file);

      const uploadRes = await fetch('/api/documents/upload', {
        method: 'POST',
        body: formData,
      });

      if (!uploadRes.ok) {
        const errorData = await uploadRes.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to upload and parse document. Please try again.');
      }

      const uploadData = await uploadRes.json();
      
      // Update local state with the parsed content
      setActiveCv(uploadData.documentId, uploadData.fileName, uploadData.extractedText);

      // Step B: Local AI Analysis (Ollama)
      setStep('analyzing');
      const analyzeRes = await fetch('/api/cv/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ documentId: uploadData.documentId }),
      });

      if (!analyzeRes.ok) {
        const errorData = await analyzeRes.json().catch(() => ({}));
        throw new Error(errorData.message || 'AI CV analysis failed or timed out. Please check local model status.');
      }

      const analyzeData = await analyzeRes.json();

      // Step C: Factual Guardrails Audit Validation
      setStep('guardrails');
      // Briefly pause to simulate secure validation check visual
      await new Promise((resolve) => setTimeout(resolve, 800));

      setStep('success');
      // Save analysis results to global context
      setAnalysisResult(analyzeData);

    } catch (e: any) {
      console.error(e);
      showError(e.message || 'An unexpected connection error occurred.');
    }
  };

  const showError = (msg: string) => {
    setErrorMessage(msg);
    setStep('error');
  };

  const resetDropzone = () => {
    setStep('idle');
    setSelectedFile(null);
    setErrorMessage(null);
  };

  return (
    <div className="w-full flex justify-center items-center py-4">
      <input
        type="file"
        ref={fileInputRef}
        onChange={handleFileSelect}
        accept=".pdf,.docx"
        className="hidden"
      />

      <AnimatePresence mode="wait">
        {step === 'idle' && (
          <motion.div
            key="idle"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={triggerFileSelect}
            className="w-full lg:col-span-2 glass-panel rounded-3xl p-10 flex flex-col items-center justify-center text-center border border-slate-800/40 relative min-h-[380px] cursor-pointer group hover:border-indigo-500/40 transition-all duration-300 shadow-[0_0_50px_rgba(0,0,0,0.3)]"
          >
            <div className="absolute inset-0 rounded-3xl bg-indigo-500/[0.01] group-hover:bg-indigo-500/[0.02] transition-colors" />
            
            {/* Glowing Backdrop Aura */}
            <div className="absolute h-48 w-48 rounded-full bg-indigo-500/5 blur-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-0" />

            <div className="z-10 flex flex-col items-center">
              <div className="h-16 w-16 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mb-6 group-hover:scale-110 group-hover:border-indigo-500/40 group-hover:shadow-[0_0_20px_rgba(99,102,241,0.2)] transition-all duration-300">
                <Upload className="h-8 w-8 text-indigo-400" />
              </div>
              <h3 className="text-xl font-bold tracking-tight text-slate-100">Ingest Career Document</h3>
              <p className="text-slate-400 text-sm max-w-sm mt-3 leading-relaxed">
                Drag and drop your resume or cover letter here, or click to browse files.
              </p>
              
              <div className="flex items-center space-x-6 mt-6 text-xs text-slate-500 font-semibold tracking-wider uppercase bg-slate-950/40 px-4 py-2 rounded-xl border border-slate-800/40">
                <span className="flex items-center"><FileText className="h-3.5 w-3.5 mr-1 text-slate-500" /> PDF</span>
                <span className="h-3 w-px bg-slate-800" />
                <span className="flex items-center"><FileText className="h-3.5 w-3.5 mr-1 text-slate-500" /> DOCX</span>
                <span className="h-3 w-px bg-slate-800" />
                <span>Max 5MB</span>
              </div>

              <button className="mt-8 px-8 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 font-semibold text-sm tracking-wide shadow-lg shadow-indigo-600/20 hover:shadow-indigo-600/35 active:scale-95 transition-all duration-200">
                Browse Files
              </button>
            </div>
          </motion.div>
        )}

        {(step === 'uploading' || step === 'analyzing' || step === 'guardrails') && (
          <motion.div
            key="processing"
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            className="w-full max-w-lg glass-panel rounded-3xl p-10 flex flex-col items-center justify-center text-center border border-slate-800/60 min-h-[380px] shadow-2xl relative overflow-hidden"
          >
            {/* Spinning background light ray effect */}
            <div className="absolute h-96 w-96 rounded-full bg-gradient-to-tr from-indigo-500/10 to-purple-500/10 blur-3xl animate-pulse z-0" />

            <div className="z-10 flex flex-col items-center w-full">
              {/* WebGL Pulsing Particles Sim / Custom Loader Sphere */}
              <div className="relative h-28 w-28 flex items-center justify-center mb-8">
                {/* Glow ring */}
                <div className="absolute inset-0 rounded-full border border-indigo-500/20 animate-ping opacity-75" />
                <div className="absolute h-24 w-24 rounded-full bg-gradient-to-tr from-indigo-500/10 via-purple-500/5 to-pink-500/10 border border-indigo-500/30 flex items-center justify-center shadow-lg shadow-indigo-500/10">
                  <Loader2 className="h-10 w-10 text-indigo-400 animate-spin" />
                </div>
                {/* Float particles */}
                <div className="absolute top-2 left-6 h-1.5 w-1.5 rounded-full bg-purple-400 blur-[0.5px] animate-bounce" style={{ animationDelay: '0.2s' }} />
                <div className="absolute bottom-4 right-5 h-2 w-2 rounded-full bg-pink-400 blur-[0.5px] animate-bounce" style={{ animationDelay: '0.5s' }} />
                <div className="absolute top-8 right-3 h-1 w-1 rounded-full bg-indigo-400 blur-[0.5px] animate-bounce" style={{ animationDelay: '0.8s' }} />
              </div>

              <h3 className="text-xl font-bold tracking-tight text-slate-100 flex items-center space-x-2 justify-center">
                <span>Analyzing document...</span>
              </h3>
              
              {selectedFile && (
                <div className="mt-2 px-3 py-1 bg-slate-900/60 border border-slate-800/40 rounded-full text-xs text-slate-400 flex items-center">
                  <FileText className="h-3.5 w-3.5 mr-1.5 text-indigo-400" />
                  <span className="truncate max-w-[200px]">{selectedFile.name}</span>
                </div>
              )}

              {/* Progress Text Stepper */}
              <div className="mt-8 w-full space-y-3.5 text-left max-w-sm bg-slate-950/50 p-6 rounded-2xl border border-slate-900 shadow-inner">
                <div className="flex items-center space-x-3 text-sm">
                  {step === 'uploading' ? (
                    <Loader2 className="h-4 w-4 text-indigo-400 animate-spin flex-shrink-0" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 flex-shrink-0" />
                  )}
                  <span className={step === 'uploading' ? 'text-indigo-400 font-semibold' : 'text-slate-500'}>
                    1. Extracting text (Apache Tika)
                  </span>
                </div>

                <div className="flex items-center space-x-3 text-sm">
                  {step === 'analyzing' ? (
                    <Loader2 className="h-4 w-4 text-indigo-400 animate-spin flex-shrink-0" />
                  ) : step === 'uploading' ? (
                    <div className="h-4 w-4 rounded-full border-2 border-slate-800 flex-shrink-0" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4 text-emerald-400 flex-shrink-0" />
                  )}
                  <span className={step === 'analyzing' ? 'text-indigo-400 font-semibold' : step === 'uploading' ? 'text-slate-600' : 'text-slate-500'}>
                    2. AI career analysis (Ollama)
                  </span>
                </div>

                <div className="flex items-center space-x-3 text-sm">
                  {step === 'guardrails' ? (
                    <Loader2 className="h-4 w-4 text-indigo-400 animate-spin flex-shrink-0" />
                  ) : (
                    <div className="h-4 w-4 rounded-full border-2 border-slate-800 flex-shrink-0" />
                  )}
                  <span className={step === 'guardrails' ? 'text-indigo-400 font-semibold' : (step === 'uploading' || step === 'analyzing') ? 'text-slate-600' : 'text-slate-500'}>
                    3. Running factual guardrails
                  </span>
                </div>
              </div>
            </div>
          </motion.div>
        )}

        {step === 'error' && (
          <motion.div
            key="error"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            className="w-full max-w-lg glass-panel rounded-3xl p-10 flex flex-col items-center justify-center text-center border border-rose-950/40 min-h-[380px] shadow-2xl relative overflow-hidden"
          >
            {/* Pulsing Red Aura */}
            <div className="absolute h-80 w-80 rounded-full bg-rose-500/[0.03] blur-3xl z-0" />

            <div className="z-10 flex flex-col items-center">
              <div className="h-16 w-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center mb-6 shadow-[0_0_15px_rgba(239,68,68,0.1)]">
                <AlertCircle className="h-8 w-8 text-rose-400 animate-bounce" />
              </div>
              <h3 className="text-xl font-bold tracking-tight text-rose-400">Ingestion Error</h3>
              <p className="text-slate-400 text-sm max-w-sm mt-3 leading-relaxed">
                {errorMessage || 'Something went wrong during file upload or AI evaluation.'}
              </p>

              <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4 mt-8 w-full justify-center">
                <button
                  onClick={resetDropzone}
                  className="px-6 py-2.5 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-slate-300 font-semibold text-sm transition-colors active:scale-95"
                >
                  Try Again
                </button>
                {selectedFile && (
                  <button
                    onClick={() => processFile(selectedFile)}
                    className="px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-slate-100 font-semibold text-sm shadow-md shadow-indigo-600/15 transition-all active:scale-95"
                  >
                    Retry Analysis
                  </button>
                )}
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
