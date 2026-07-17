import React, { useEffect, useState } from 'react';
import { 
  LayoutDashboard, 
  Briefcase, 
  Sparkles, 
  LineChart, 
  FileText, 
  Trash2
} from 'lucide-react';
import { useCv } from '../context/CvContext';

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
    { id: 'dashboard', label: 'CV Review', icon: LayoutDashboard },
    { id: 'matcher', label: 'Job Match', icon: Briefcase },
    { id: 'rewriter', label: 'Bullet Optimizer', icon: Sparkles },
  ] as const;

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-[var(--color-background)] text-[var(--color-text-primary)] antialiased font-sans relative">
      
      {/* 1. Permanent Navigation Sidebar */}
      <aside className="w-64 h-full flex flex-col justify-between p-6 border-r border-[var(--color-border)] bg-[var(--color-surface)] z-10 select-none">
        
        {/* Top Logo and Navigation Links */}
        <div className="space-y-8">
          
          {/* Main Logo: Geometric placeholder and name */}
          <div className="flex items-center space-x-3 px-1">
            <div className="h-9 w-9 rounded-lg bg-[var(--color-primary)] flex items-center justify-center text-white font-black text-sm shadow-sm flex-shrink-0">
              RM
            </div>
            <div className="min-w-0">
              <h1 className="text-sm font-bold tracking-tight text-[var(--color-primary)] leading-tight truncate">
                Resume Match Helper
              </h1>
              <p className="text-[9px] text-[var(--color-text-muted)] tracking-wider font-semibold uppercase">Workspace</p>
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
                  className={`w-full flex items-center space-x-3 px-4 py-2.5 rounded-lg transition-all duration-200 text-sm font-medium tracking-wide border-l-4 ${
                    isActive 
                      ? 'bg-[var(--color-surface-selected)] text-[var(--color-primary)] border-[var(--color-accent)]' 
                      : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] border-transparent'
                  }`}
                >
                  <Icon className={`h-4.5 w-4.5 flex-shrink-0 ${isActive ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)]'}`} />
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>
        </div>

        {/* Bottom Connection Status & Active Resume File */}
        <div className="space-y-5 border-t border-[var(--color-border)] pt-6">
          
          {/* Selected CV Context Status */}
          {activeFileName ? (
            <div className="flex flex-col p-3 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] relative">
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-2 min-w-0">
                  <FileText className="h-4 w-4 text-[var(--color-primary)] flex-shrink-0" />
                  <span className="text-xs font-semibold text-[var(--color-text-primary)] truncate" title={activeFileName}>
                    {activeFileName}
                  </span>
                </div>
                <button 
                  onClick={clearActiveCv}
                  className="text-[var(--color-text-muted)] hover:text-[var(--color-error)] transition-colors duration-200 flex-shrink-0 ml-1"
                  title="Clear active document"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
              <div className="flex items-center justify-between mt-2 text-[10px] text-[var(--color-text-secondary)] font-medium">
                <span className="text-[var(--color-success)] font-semibold uppercase tracking-wider">Ready</span>
                <span>{activeFileName.split('.').pop()?.toUpperCase() || 'CV'}</span>
              </div>
            </div>
          ) : (
            <div className="p-3 rounded-lg border border-dashed border-[var(--color-border)] bg-[var(--color-surface-muted)] text-center">
              <span className="text-[10px] font-semibold text-[var(--color-text-muted)] tracking-wider uppercase">No Active Document</span>
            </div>
          )}

          {/* System Section visually separated */}
          <div className="pt-2 border-t border-[var(--color-border)]">
            <span className="px-1 text-[9px] font-bold text-[var(--color-text-muted)] tracking-wider uppercase block mb-1.5">
              System
            </span>
            <button
              onClick={() => setActiveTab('admin')}
              className={`w-full flex items-center space-x-3 px-4 py-2 rounded-lg transition-all duration-200 text-xs font-medium tracking-wide border-l-4 ${
                activeTab === 'admin' 
                  ? 'bg-[var(--color-surface-selected)] text-[var(--color-primary)] border-[var(--color-accent)]' 
                  : 'text-[var(--color-text-secondary)] hover:text-[var(--color-text-primary)] hover:bg-[var(--color-surface-hover)] border-transparent'
              }`}
            >
              <LineChart className={`h-4 w-4 flex-shrink-0 ${activeTab === 'admin' ? 'text-[var(--color-primary)]' : 'text-[var(--color-text-secondary)]'}`} />
              <span>System Monitor</span>
            </button>
          </div>

          {/* Backend Connection Diagnostics */}
          <div className="space-y-2 border-t border-[var(--color-border)] pt-4">
            <div className="flex flex-col p-2.5 rounded-lg bg-[var(--color-surface-muted)] border border-[var(--color-border)] text-[11px] leading-relaxed">
              <div className="flex items-center justify-between">
                <span className="font-semibold text-[var(--color-text-secondary)]">Local Processing</span>
                <span className={`font-bold uppercase text-[9px] ${backendConnected ? 'text-[var(--color-success)]' : 'text-[var(--color-text-muted)]'}`}>
                  {backendConnected ? 'Active' : 'Inactive'}
                </span>
              </div>
              <p className="text-[10px] text-[var(--color-text-muted)] mt-1">
                Your CV and job description are processed entirely on your local machine.
              </p>
            </div>

            <div className="flex items-center justify-between px-1 text-[11px]">
              <span className="text-[var(--color-text-secondary)] font-medium">Service Status</span>
              <div className="flex items-center space-x-1.5">
                {backendConnected === null ? (
                  <span className="text-[10px] text-[var(--color-text-muted)]">Checking...</span>
                ) : backendConnected ? (
                  <span className="text-[10px] font-bold text-[var(--color-success)] tracking-wide uppercase flex items-center">
                    <span className="h-1.5 w-1.5 rounded-full bg-[var(--color-success)] mr-1.5" />
                    Private Ready
                  </span>
                ) : (
                  <span className="text-[10px] font-bold text-[var(--color-error)] tracking-wide uppercase flex items-center">
                    <span className="h-1.5 w-1.5 rounded-full bg-[var(--color-error)] mr-1.5" />
                    Unavailable
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

      </aside>

      {/* 2. Main Tab View Container */}
      <main className="flex-grow h-full overflow-y-auto bg-[var(--color-background)] relative p-8 z-10">
        <div className="max-w-[1280px] mx-auto h-full flex flex-col">
          {children}
        </div>
      </main>

    </div>
  );
}
