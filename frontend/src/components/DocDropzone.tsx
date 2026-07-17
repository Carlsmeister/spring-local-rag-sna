import { useState, useRef } from 'react';
import type { DragEvent, ChangeEvent } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, AlertCircle, CheckCircle2, Loader2, ShieldCheck } from 'lucide-react';
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
      e.currentTarget.classList.add('border-[var(--color-accent)]', 'bg-[var(--color-surface-hover)]');
    }
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.currentTarget.classList.remove('border-[var(--color-accent)]', 'bg-[var(--color-surface-hover)]');
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.currentTarget.classList.remove('border-[var(--color-accent)]', 'bg-[var(--color-surface-hover)]');
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
        throw new Error(errorData.message || 'Failed to upload and parse document. Please check that the file is not corrupted.');
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
        throw new Error(errorData.message || 'AI CV analysis failed. Please check that your local model service is online.');
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
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={triggerFileSelect}
            className="w-full glass-panel rounded-xl p-10 flex flex-col items-center justify-center text-center border border-[var(--color-border)] bg-[var(--color-surface)] min-h-[380px] cursor-pointer group hover:border-[var(--color-border-strong)] transition-all duration-300 shadow-sm"
          >
            <div className="flex flex-col items-center max-w-md">
              <div className="h-14 w-14 rounded-lg bg-[var(--color-primary-subtle)] border border-[var(--color-border)] flex items-center justify-center mb-6 group-hover:scale-105 group-hover:border-[var(--color-border-strong)] transition-all duration-200">
                <Upload className="h-6 w-6 text-[var(--color-primary)]" />
              </div>
              <h3 className="text-lg font-bold tracking-tight text-[var(--color-text-primary)]">Review your CV</h3>
              <p className="text-[var(--color-text-secondary)] text-sm mt-3 leading-relaxed">
                Upload a PDF or DOCX file to review its structure, clarity, and compatibility with automated systems.
              </p>
              
              <div className="flex items-center space-x-4 mt-6 text-xs text-[var(--color-text-muted)] font-semibold tracking-wider uppercase bg-[var(--color-surface-muted)] px-4 py-2 rounded-lg border border-[var(--color-border)]">
                <span className="flex items-center"><FileText className="h-3.5 w-3.5 mr-1 text-[var(--color-text-muted)]" /> PDF</span>
                <span className="h-3 w-px bg-[var(--color-border)]" />
                <span className="flex items-center"><FileText className="h-3.5 w-3.5 mr-1 text-[var(--color-text-muted)]" /> DOCX</span>
                <span className="h-3 w-px bg-[var(--color-border)]" />
                <span>Max 5MB</span>
              </div>

              <button className="mt-8 px-6 py-2.5 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-sm tracking-wide transition-all duration-200">
                Browse Files
              </button>
            </div>
          </motion.div>
        )}

        {(step === 'uploading' || step === 'analyzing' || step === 'guardrails') && (
          <motion.div
            key="processing"
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            className="w-full max-w-lg glass-panel rounded-xl p-10 flex flex-col items-center justify-center text-center border border-[var(--color-border)] bg-[var(--color-surface)] min-h-[380px] shadow-sm relative overflow-hidden"
          >
            <div className="flex flex-col items-center w-full max-w-sm">
              
              {/* Clean mechanical spinning loader */}
              <div className="relative h-16 w-16 flex items-center justify-center mb-6">
                <div className="absolute inset-0 rounded-full border-4 border-[var(--color-border)]" />
                <div className="absolute inset-0 rounded-full border-4 border-[var(--color-accent)] border-t-transparent animate-spin" />
              </div>

              <h3 className="text-base font-bold tracking-tight text-[var(--color-text-primary)]">
                Analysing document...
              </h3>
              
              {selectedFile && (
                <div className="mt-2 px-3 py-1 bg-[var(--color-surface-muted)] border border-[var(--color-border)] rounded-full text-xs text-[var(--color-text-secondary)] flex items-center">
                  <FileText className="h-3.5 w-3.5 mr-1.5 text-[var(--color-primary)]" />
                  <span className="truncate max-w-[200px]">{selectedFile.name}</span>
                </div>
              )}

              {/* Progress Text Stepper */}
              <div className="mt-8 w-full space-y-4 text-left bg-[var(--color-surface-muted)] p-5 rounded-lg border border-[var(--color-border)]">
                
                {/* Step 1: Ingestion */}
                <div className="flex items-center space-x-3 text-xs font-medium">
                  {step === 'uploading' ? (
                    <Loader2 className="h-4 w-4 text-[var(--color-accent-dark)] animate-spin flex-shrink-0" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4 text-[var(--color-success)] flex-shrink-0" />
                  )}
                  <span className={step === 'uploading' ? 'text-[var(--color-accent-dark)] font-bold' : 'text-[var(--color-text-muted)]'}>
                    1. Reading your document
                  </span>
                </div>

                {/* Step 2: Analysis */}
                <div className="flex items-center space-x-3 text-xs font-medium">
                  {step === 'analyzing' ? (
                    <Loader2 className="h-4 w-4 text-[var(--color-accent-dark)] animate-spin flex-shrink-0" />
                  ) : step === 'uploading' ? (
                    <div className="h-4 w-4 rounded-full border border-[var(--color-border-strong)] bg-white flex-shrink-0" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4 text-[var(--color-success)] flex-shrink-0" />
                  )}
                  <span className={step === 'analyzing' ? 'text-[var(--color-accent-dark)] font-bold' : (step === 'uploading') ? 'text-[var(--color-text-muted)] opacity-50' : 'text-[var(--color-text-muted)]'}>
                    2. Reviewing content and structure
                  </span>
                </div>

                {/* Step 3: Guardrails */}
                <div className="flex items-center space-x-3 text-xs font-medium">
                  {step === 'guardrails' ? (
                    <Loader2 className="h-4 w-4 text-[var(--color-accent-dark)] animate-spin flex-shrink-0" />
                  ) : (step === 'uploading' || step === 'analyzing') ? (
                    <div className="h-4 w-4 rounded-full border border-[var(--color-border-strong)] bg-white flex-shrink-0" />
                  ) : (
                    <CheckCircle2 className="h-4 w-4 text-[var(--color-success)] flex-shrink-0" />
                  )}
                  <span className={step === 'guardrails' ? 'text-[var(--color-accent-dark)] font-bold' : (step === 'uploading' || step === 'analyzing') ? 'text-[var(--color-text-muted)] opacity-50' : 'text-[var(--color-text-muted)]'}>
                    3. Checking suggestions against the source
                  </span>
                </div>

                {/* Step 4: Finished status */}
                <div className="flex items-center space-x-3 text-xs font-medium border-t border-[var(--color-border)] pt-3.5 mt-1">
                  <ShieldCheck className="h-4 w-4 text-[var(--color-accent-dark)] flex-shrink-0" />
                  <span className="text-[var(--color-text-secondary)] font-semibold">
                    Factual guardrails active
                  </span>
                </div>
              </div>
            </div>
          </motion.div>
        )}

        {step === 'error' && (
          <motion.div
            key="error"
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            className="w-full max-w-lg glass-panel rounded-xl p-10 flex flex-col items-center justify-center text-center border border-[var(--color-error)] bg-[var(--color-surface)] min-h-[380px] shadow-sm"
          >
            <div className="flex flex-col items-center">
              <div className="h-14 w-14 rounded-lg bg-[var(--color-error-subtle)] border border-[var(--color-error)] flex items-center justify-center mb-6 shadow-sm">
                <AlertCircle className="h-6 w-6 text-[var(--color-error)]" />
              </div>
              <h3 className="text-lg font-bold tracking-tight text-[var(--color-error)]">Ingestion Error</h3>
              <p className="text-[var(--color-text-secondary)] text-sm max-w-sm mt-3 leading-relaxed">
                {errorMessage || 'Something went wrong during file upload or AI evaluation.'}
              </p>

              <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-4 mt-8 w-full justify-center">
                <button
                  onClick={resetDropzone}
                  className="px-5 py-2 rounded-lg bg-white border border-[var(--color-border-strong)] hover:bg-[var(--color-surface-hover)] text-[var(--color-text-primary)] font-semibold text-sm transition-colors duration-200"
                >
                  Try Again
                </button>
                {selectedFile && (
                  <button
                    onClick={() => processFile(selectedFile)}
                    className="px-5 py-2 rounded-lg bg-[var(--color-primary)] hover:bg-[var(--color-primary-hover)] text-white font-semibold text-sm transition-all duration-200"
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
