import type { ReactNode } from 'react'
import Sidebar from '../components/Sidebar'

type Props = {
  title: string
  subtitle?: string
  actions?: ReactNode
  children: ReactNode
}

export default function AppLayout({ title, subtitle, actions, children }: Props) {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <header className="topbar">
          <div className="topbar-left">
            <span className="topbar-title">{title}</span>
            {subtitle && <span className="topbar-subtitle">{subtitle}</span>}
          </div>
          {actions && <div className="topbar-actions">{actions}</div>}
        </header>
        <main className="page-content">{children}</main>
      </div>
    </div>
  )
}
