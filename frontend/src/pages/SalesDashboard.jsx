// [MIGRATION] AS-IS: Nexacro Form - SalesDashboard.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise / Chart: ECharts

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import CommonGrid from '../components/CommonGrid';
import CommonChart from '../components/CommonChart';
import SearchBar from '../components/SearchBar';
import { getSalesList, getSalesSummary } from '../api/salesApi';
import { createColumnDef, createNumberColumnDef } from '../utils/gridUtils';
import { fn_getCurrentYear, fn_numberFormat, fn_alert } from '../utils/commonUtils';

/**
 * 매출 대시보드 화면
 * Nexacro 패턴: Grid + Chart 연동, Dataset 바인딩
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - fn_search → handleSearch (useCallback + Axios GET)
 *
 * Nexacro Chart 변환:
 * - BarChart → type: 'bar'
 * - LineChart → type: 'line'
 * - PieChart → type: 'pie'
 */
function SalesDashboard() {
  const [salesData, setSalesData] = useState([]);
  const [summaryData, setSummaryData] = useState(null);
  const [loading, setLoading] = useState(false);

  // 검색 조건 (Nexacro 검색 Dataset 대응)
  const [searchCondition, setSearchCondition] = useState({
    year: fn_getCurrentYear(),
    quarter: '',
  });

  // AG Grid 컬럼 정의
  const columnDefs = [
    createColumnDef('deptName', '부서명', { width: 150 }),
    createColumnDef('quarter', '분기', { width: 80 }),
    createColumnDef('productName', '상품명', { width: 200 }),
    createNumberColumnDef('salesAmount', '매출액', { width: 150 }),
    createNumberColumnDef('salesCount', '판매수량', { width: 120 }),
    createNumberColumnDef('salesAvg', '평균단가', { width: 120 }),
    createColumnDef('growthRate', '성장률(%)', {
      width: 100,
      valueFormatter: (params) => (params.value ? `${params.value}%` : ''),
      cellClassRules: {
        'cell-positive': (params) => params.value > 0,
        'cell-negative': (params) => params.value < 0,
      },
    }),
  ];

  // fn_init 대응 (검색 버튼 없이 최초 1회 조회)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { handleSearch(); }, []);

  // fn_search 대응
  const handleSearch = useCallback(async () => {
    setLoading(true);
    try {
      const [salesResponse, summaryResponse] = await Promise.all([
        getSalesList(searchCondition),
        getSalesSummary({ year: searchCondition.year }),
      ]);
      setSalesData(salesResponse.data);
      setSummaryData(summaryResponse.data);
    } catch (error) {
      console.error('매출 데이터 조회 실패:', error);
      fn_alert('매출 데이터 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [searchCondition]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchCondition((prev) => ({ ...prev, [name]: value }));
  };

  // ECharts Bar 차트 옵션 (Nexacro BarChart → type: 'bar')
  const barChartOption = useMemo(() => {
    if (!summaryData) return {};
    return {
      title: { text: '분기별 매출 현황', left: 'center' },
      tooltip: { trigger: 'axis', formatter: (params) => {
        return params.map(p => `${p.name}: ${fn_numberFormat(p.value)}원`).join('<br/>');
      }},
      xAxis: {
        type: 'category',
        data: summaryData.quarters || ['1분기', '2분기', '3분기', '4분기'],
      },
      yAxis: {
        type: 'value',
        axisLabel: { formatter: (value) => `${fn_numberFormat(value / 1000000)}M` },
      },
      series: [
        {
          name: '매출액',
          type: 'bar',  // Nexacro BarChart → type: 'bar'
          data: summaryData.barData || [],
          itemStyle: { color: '#4589d9' },
          label: {
            show: true,
            position: 'top',
            formatter: (params) => `${fn_numberFormat(params.value / 1000000)}M`,
          },
        },
      ],
    };
  }, [summaryData]);

  // ECharts Line 차트 옵션 (Nexacro LineChart → type: 'line')
  const lineChartOption = useMemo(() => {
    if (!summaryData) return {};
    return {
      title: { text: '월별 매출 추이', left: 'center' },
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: summaryData.months || ['1월', '2월', '3월', '4월', '5월', '6월',
                                     '7월', '8월', '9월', '10월', '11월', '12월'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '매출액',
          type: 'line',  // Nexacro LineChart → type: 'line'
          data: summaryData.lineData || [],
          smooth: true,
          areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#52c41a' },
        },
        {
          name: '전년 동기',
          type: 'line',
          data: summaryData.prevLineData || [],
          smooth: true,
          lineStyle: { type: 'dashed' },
          itemStyle: { color: '#ff7875' },
        },
      ],
    };
  }, [summaryData]);

  // ECharts Pie 차트 옵션 (Nexacro PieChart → type: 'pie')
  const pieChartOption = useMemo(() => {
    if (!summaryData) return {};
    return {
      title: { text: '부서별 매출 비중', left: 'center' },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      series: [
        {
          name: '매출 비중',
          type: 'pie',  // Nexacro PieChart → type: 'pie'
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          data: summaryData.pieData || [],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    };
  }, [summaryData]);

  return (
    <div className="page-wrapper">
      <h2 className="page-title">매출 대시보드</h2>

      {/* 검색 조건 */}
      <SearchBar onSearch={handleSearch}>
        <div className="search-row">
          <div className="search-item">
            <label>연도</label>
            <select name="year" value={searchCondition.year} onChange={handleInputChange}>
              {[2024, 2023, 2022, 2021].map((year) => (
                <option key={year} value={year}>{year}년</option>
              ))}
            </select>
          </div>
          <div className="search-item">
            <label>분기</label>
            <select name="quarter" value={searchCondition.quarter} onChange={handleInputChange}>
              <option value="">전체</option>
              <option value="1">1분기</option>
              <option value="2">2분기</option>
              <option value="3">3분기</option>
              <option value="4">4분기</option>
            </select>
          </div>
        </div>
      </SearchBar>

      {/* 차트 영역 (Nexacro Chart → ECharts) */}
      <div className="chart-section">
        <div className="chart-row">
          <div className="chart-col">
            <CommonChart option={barChartOption} height="300px" loading={loading} />
          </div>
          <div className="chart-col">
            <CommonChart option={pieChartOption} height="300px" loading={loading} />
          </div>
        </div>
        <div className="chart-row">
          <div className="chart-col-full">
            <CommonChart option={lineChartOption} height="280px" loading={loading} />
          </div>
        </div>
      </div>

      {/* Grid 영역 */}
      <div className="grid-section">
        <div className="section-header">
          <h3>매출 상세</h3>
          <span className="record-count">총 {salesData.length}건</span>
        </div>
        {loading ? (
          <div className="loading-spinner">데이터 로딩 중...</div>
        ) : (
          <CommonGrid
            columnDefs={columnDefs}
            rowData={salesData}
            height="350px"
            showExcelButton={true}
            excelFileName="매출현황"
          />
        )}
      </div>
    </div>
  );
}

export default SalesDashboard;
