import React, { useEffect, useState } from 'react';
import { 
  LayoutDashboard, 
  Briefcase, 
  Sparkles, 
  LineChart, 
  FileText, 
  CloudLightning, 
  CheckCircle2, 
  XCircle,
  Trash2
} from 'lucide-react';
import { useCv } from '../context/CvContext';
import ShinyText from './animations/ShinyText';

type TabType = 'dashboard' | 'matcher' | 'rewriter' | 'admin';

interface SidebarShellProps {
  activeTab: TabType;
  setActiveTab: (tab: TabType) => void;
  children: React.ReactNode;
}

export default function SidebarShell({ activeTab, setActiveTab, children }: SidebarShellProps) {
  const { activeFileName, clearActiveCv } = useCv();
  const [backendConnected, setBackendConnected] = useState<boolean | null>(null);

  // Check backend server connection status on mount
  useEffect(() => {
    const checkConnection = async () => {
      try {
        const res = await fetch('/api/chat?message=Ping');
        if (res.ok) {
          setBackendConnected(true);
        } else {
          setBackendConnected(false);
        }
      } catch (e) {
        setBackendConnected(false);
      }
    };
    checkConnection();
    const interval = setInterval(checkConnection, 15000);
    return () => clearInterval(interval);
  }, []);

  const navItems = [
    { id: 'dashboard', label: 'Scan Dashboard', icon: LayoutDashboard },
    { id: 'matcher', label: 'Job Matcher', icon: Briefcase },
    { id: 'rewriter', label: 'Bullet Optimizer', icon: Sparkles },
    { id: 'admin', label: 'Telemetry Monitor', icon: LineChart },
  ] as const;

  return (
    <div className="flex h-screen w-screen overflow-hidden text-slate-100 antialiased font-sans relative">
      
      {/* 1. Permanent Navigation Sidebar */}
      <aside className="w-72 h-full flex flex-col justify-between p-6 border-r border-slate-800/60 bg-slate-950/70 backdrop-blur-xl z-10 select-none">
        
        {/* Top Logo and Navigation Links */}
        <div className="space-y-8">
          
          {/* Main Logo Swept with Shimmer */}
          <div className="flex items-center space-x-3 px-2">
            <div className="h-9 w-9 rounded-xl bg-gradient-to-tr from-indigo-500 via-purple-500 to-pink-500 flex items-center justify-center shadow-lg shadow-indigo-500/25">
              <CloudLightning className="h-5 w-5 text-white animate-pulse" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight">
                <ShinyText text="local-ai" className="font-extrabold" speed={6} />
              </h1>
              <p className="text-[10px] text-slate-500 tracking-wider font-semibold uppercase -mt-1">Career Document Engine</p>
            </div>
          </div>

          {/* Navigation Tab Selections */}
          <nav className="space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-300 text-sm font-medium tracking-wide ${
                    isActive 
                      ? 'bg-indigo-600/20 text-indigo-400 border-l-4 border-indigo-500 shadow-md shadow-indigo-600/5' 
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900/50 border-l-4 border-transparent'
                  }`}
                >
                  <Icon className={`h-5 w-5 transition-transform duration-300 ${isActive ? 'scale-110 text-indigo-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        {/* Bottom Connection Status & Active Resume File */}
        <div className="space-y-4 border-t border-slate-800/60 pt-6">
          
          {/* Selected CV Context Status */}
          {activeFileName ? (
            <div className="flex flex-col p-3 rounded-xl bg-slate-900/50 border border-slate-800/40 relative group">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-2 min-w-0">
                  <FileText className="h-4 w-4 text-indigo-400 flex-shrink-0" />
                  <span className="text-xs font-semibold text-slate-300 truncate" title={activeFileName}>
                    {activeFileName}
                  </span>
                </div>
                <button 
                  onClick={clearActiveCv}
                  className="text-slate-500 hover:text-rose-400 transition-colors duration-200 flex-shrink-0 ml-1"
                  title="Clear document context"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
              <span className="text-[10px] font-medium text-indigo-400 tracking-wider uppercase mt-1">CV Loaded</span>
            </div>
          ) : (
            <div className="p-3 rounded-xl bg-slate-900/20 border border-dashed border-slate-800/40 text-center">
              <span className="text-[10px] font-semibold text-slate-500 tracking-wider uppercase">No CV Active</span>
            </div>
          )}

          {/* Backend Connection Diagnostics */}
          <div className="flex items-center justify-between px-2">
            <span className="text-xs font-medium text-slate-500 tracking-wider uppercase">Service Connection</span>
            <div className="flex items-center space-x-1.5">
              {backendConnected === null ? (
                <div className="h-2 w-2 rounded-full bg-amber-500 animate-pulse" />
              ) : backendConnected ? (
                <div className="flex items-center space-x-1">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" />
                  <span className="text-[10px] font-bold text-emerald-500 tracking-wide uppercase">Active</span>
                </div>
              ) : (
                <div className="flex items-center space-x-1">
                  <XCircle className="h-3.5 w-3.5 text-rose-500" />
                  <span className="text-[10px] font-bold text-rose-500 tracking-wide uppercase">Offline</span>
                </div>
              )}
            </div>
          </div>
        </div>

      </aside>

      {/* 2. Main Tab View Container */}
      <main className="flex-1 h-full overflow-y-auto bg-transparent relative p-8 z-10">
        <div className="max-w-7xl mx-auto h-full flex flex-col">
          {children}
        </div>
      </main>

    </div>
  );
}
