// [MIGRATION] AS-IS: Nexacro Chart 컴포넌트
// [TO-BE]: Apache ECharts 래퍼 컴포넌트
// Chart: ECharts (echarts-for-react)

import React, { useMemo } from 'react';
import ReactECharts from 'echarts-for-react';

/**
 * ECharts 래퍼 컴포넌트 (Nexacro Chart 대응)
 * @param {Object} option - ECharts 옵션 설정
 * @param {string} height - 차트 높이
 * @param {Object} onEvents - 이벤트 핸들러 맵 (Nexacro Chart 이벤트 대응)
 * @param {boolean} loading - 로딩 상태
 *
 * Nexacro Chart 타입 → ECharts series.type 매핑:
 * - BarChart → type: 'bar'
 * - LineChart → type: 'line'
 * - PieChart → type: 'pie'
 */
function CommonChart({ option = {}, height = '350px', onEvents = {}, loading = false }) {
  const defaultOption = useMemo(
    () => ({
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        type: 'scroll',
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',
        containLabel: true,
      },
      ...option,
    }),
    [option]
  );

  const loadingOption = {
    text: '데이터 로딩 중...',
    color: '#4589d9',
    textColor: '#000',
    maskColor: 'rgba(255, 255, 255, 0.8)',
    zlevel: 0,
  };

  return (
    <div className="common-chart-wrapper">
      <ReactECharts
        option={defaultOption}
        style={{ height, width: '100%' }}
        showLoading={loading}
        loadingOption={loadingOption}
        onEvents={onEvents}
        notMerge={false}
        lazyUpdate={true}
      />
    </div>
  );
}

export default CommonChart;
