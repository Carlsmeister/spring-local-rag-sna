import React, { createContext, useContext, useState, useEffect } from 'react';

export interface CvAnalysis {
  atsScore: number;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  rawResponse?: string;
}

interface CvContextType {
  activeCvId: string | null;
  parsedContent: string | null;
  analysisResult: CvAnalysis | null;
  activeFileName: string | null;
  setActiveCv: (id: string, fileName: string, content: string) => void;
  setAnalysisResult: (res: CvAnalysis | null) => void;
  clearActiveCv: () => void;
}

const CvContext = createContext<CvContextType | undefined>(undefined);

export function CvProvider({ children }: { children: React.ReactNode }) {
  const [activeCvId, setActiveCvId] = useState<string | null>(() => 
    localStorage.getItem('active_cv_id')
  );
  const [parsedContent, setParsedContent] = useState<string | null>(() => 
    localStorage.getItem('parsed_cv_content')
  );
  const [activeFileName, setActiveFileName] = useState<string | null>(() => 
    localStorage.getItem('active_cv_filename')
  );
  const [analysisResult, setAnalysisResultState] = useState<CvAnalysis | null>(() => {
    const saved = localStorage.getItem('active_cv_analysis');
    return saved ? JSON.parse(saved) : null;
  });

  const setActiveCv = (id: string, fileName: string, content: string) => {
    setActiveCvId(id);
    setActiveFileName(fileName);
    setParsedContent(content);
    setAnalysisResultState(null); // Reset analysis when new CV is uploaded
  };

  const setAnalysisResult = (res: CvAnalysis | null) => {
    setAnalysisResultState(res);
  };

  const clearActiveCv = () => {
    setActiveCvId(null);
    setActiveFileName(null);
    setParsedContent(null);
    setAnalysisResultState(null);
  };

  // Sync state with LocalStorage
  useEffect(() => {
    if (activeCvId) {
      localStorage.setItem('active_cv_id', activeCvId);
    } else {
      localStorage.removeItem('active_cv_id');
    }
  }, [activeCvId]);

  useEffect(() => {
    if (activeFileName) {
      localStorage.setItem('active_cv_filename', activeFileName);
    } else {
      localStorage.removeItem('active_cv_filename');
    }
  }, [activeFileName]);

  useEffect(() => {
    if (parsedContent) {
      localStorage.setItem('parsed_cv_content', parsedContent);
    } else {
      localStorage.removeItem('parsed_cv_content');
    }
  }, [parsedContent]);

  useEffect(() => {
    if (analysisResult) {
      localStorage.setItem('active_cv_analysis', JSON.stringify(analysisResult));
    } else {
      localStorage.removeItem('active_cv_analysis');
    }
  }, [analysisResult]);

  return (
    <CvContext.Provider value={{
      activeCvId,
      parsedContent,
      analysisResult,
      activeFileName,
      setActiveCv,
      setAnalysisResult,
      clearActiveCv
    }}>
      {children}
    </CvContext.Provider>
  );
}

export function useCv() {
  const context = useContext(CvContext);
  if (context === undefined) {
    throw new Error('useCv must be used within a CvProvider');
  }
  return context;
}
