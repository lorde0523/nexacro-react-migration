// [MIGRATION] AS-IS: Nexacro Application Router
// [TO-BE]: React Router v5 기반 라우팅 (v6 사용 금지)

import React from 'react';
import { BrowserRouter as Router, Switch, Route, Redirect, NavLink } from 'react-router-dom';
import EmployeeList from './pages/EmployeeList';
import MasterDetail from './pages/MasterDetail';
import EmployeeCrud from './pages/EmployeeCrud';
import SalesDashboard from './pages/SalesDashboard';
import BoardList from './pages/BoardList';
import BoardWrite from './pages/BoardWrite';
import CodeSearchPopup from './pages/CodeSearchPopup';

function App() {
  return (
    <Router>
      <div className="app-layout">
        <nav className="app-nav">
          <ul>
            <li><NavLink activeClassName="active" to="/employees">사원 목록</NavLink></li>
            <li><NavLink activeClassName="active" to="/master-detail">마스터-디테일</NavLink></li>
            <li><NavLink activeClassName="active" to="/employee-crud">사원 CRUD</NavLink></li>
            <li><NavLink activeClassName="active" to="/sales-dashboard">매출 대시보드</NavLink></li>
            <li><NavLink activeClassName="active" to="/board">게시판</NavLink></li>
            <li><NavLink activeClassName="active" to="/code-search">코드 검색</NavLink></li>
          </ul>
        </nav>
        <main className="app-main">
          <Switch>
            <Route exact path="/" render={() => <Redirect to="/employees" />} />
            <Route path="/employees" component={EmployeeList} />
            <Route path="/master-detail" component={MasterDetail} />
            <Route path="/employee-crud" component={EmployeeCrud} />
            <Route path="/sales-dashboard" component={SalesDashboard} />
            <Route exact path="/board" component={BoardList} />
            <Route path="/board/write" component={BoardWrite} />
            <Route path="/board/edit/:id" component={BoardWrite} />
            <Route path="/code-search" component={CodeSearchPopup} />
          </Switch>
        </main>
      </div>
    </Router>
  );
}

export default App;
