<template>
  <div class="stats-page">
    <!-- KPI 概览 -->
    <div class="kpi-row">
      <div class="kpi-card">
        <div class="kpi-icon green">📊</div>
        <div class="kpi-body">
          <div class="kpi-value">{{ kpi.orders }}</div>
          <div class="kpi-label">累计订单量</div>
        </div>
        <div class="kpi-glow"></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon blue">🏕️</div>
        <div class="kpi-body">
          <div class="kpi-value">{{ kpi.stores }}</div>
          <div class="kpi-label">在营网点数</div>
        </div>
        <div class="kpi-glow blue"></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon orange">🎒</div>
        <div class="kpi-body">
          <div class="kpi-value">{{ kpi.equipments }}</div>
          <div class="kpi-label">在租装备种类</div>
        </div>
        <div class="kpi-glow orange"></div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card camp-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">各网点订单数量</div>
            <div class="chart-sub">按网点维度统计订单占比</div>
          </div>
        </div>
        <div ref="storeChartRef" class="chart"></div>
      </div>

      <div class="chart-card camp-card">
        <div class="chart-head">
          <div>
            <div class="chart-title">装备租赁频次 TOP10</div>
            <div class="chart-sub">租赁次数最高的装备</div>
          </div>
        </div>
        <div ref="equipChartRef" class="chart"></div>
      </div>
    </div>

    <div class="chart-card camp-card wide">
      <div class="chart-head">
        <div>
          <div class="chart-title">近 30 天订单趋势</div>
          <div class="chart-sub">每日新增订单数量变化</div>
        </div>
      </div>
      <div ref="trendChartRef" class="chart trend"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { getOrderByStore, getEquipmentRent, getOrderTrend } from '@/api/admin'

const storeChartRef = ref()
const equipChartRef = ref()
const trendChartRef = ref()
let charts = []

const kpi = reactive({ orders: 0, stores: 0, equipments: 0 })

const AXIS_COLOR = '#8b9a94'
const GRID_LINE = 'rgba(15,31,26,0.06)'

function baseTooltip(trigger) {
  return {
    trigger,
    backgroundColor: 'rgba(15,31,26,0.92)',
    borderWidth: 0,
    padding: [10, 14],
    textStyle: { color: '#fff', fontSize: 12.5 },
    extraCssText: 'border-radius:12px;box-shadow:0 12px 32px rgba(16,42,33,.25);'
  }
}

onMounted(async () => {
  const [storeData, equipData, trendData] = await Promise.all([
    getOrderByStore(),
    getEquipmentRent(),
    getOrderTrend()
  ])
  const sd = storeData || []
  const ed = equipData || []
  const td = trendData || []

  kpi.stores = sd.length
  kpi.orders = sd.reduce((sum, d) => sum + Number(d.value || 0), 0)
  kpi.equipments = ed.length

  renderStore(sd)
  renderEquip(ed)
  renderTrend(td)

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
})

function handleResize() {
  charts.forEach((c) => c.resize())
}

function renderStore(data) {
  const chart = echarts.init(storeChartRef.value)
  charts.push(chart)
  const total = data.reduce((s, d) => s + Number(d.value || 0), 0)
  chart.setOption({
    tooltip: baseTooltip('item'),
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      itemGap: 18,
      textStyle: { color: '#43564f', fontSize: 12 }
    },
    color: ['#16b876', '#3ddc97', '#7aa7ff', '#ffb547', '#ff8a8f', '#a78bfa'],
    series: [
      {
        type: 'pie',
        radius: ['52%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 4, borderRadius: 8 },
        label: { show: false },
        labelLine: { show: false },
        data: data.map((d) => ({ name: d.name, value: d.value })),
        emphasis: {
          scaleSize: 6,
          label: {
            show: true,
            formatter: '{b}\n{c} 单',
            fontSize: 13,
            fontWeight: 700,
            color: '#0f1f1a'
          }
        }
      }
    ],
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '37%',
        style: {
          text: String(total),
          fontSize: 28,
          fontWeight: 800,
          fill: '#0f1f1a',
          textAlign: 'center'
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '49%',
        style: { text: '总订单', fontSize: 11.5, fill: '#8b9a94', textAlign: 'center' }
      }
    ]
  })
}

function renderEquip(data) {
  const chart = echarts.init(equipChartRef.value)
  charts.push(chart)
  const sorted = [...data].sort((a, b) => Number(a.value) - Number(b.value))
  chart.setOption({
    tooltip: baseTooltip('axis'),
    grid: { left: 10, right: 30, top: 10, bottom: 10, containLabel: true },
    xAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: GRID_LINE } },
      axisLabel: { color: AXIS_COLOR, fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: sorted.map((d) => d.name),
      axisLabel: { color: '#43564f', fontSize: 12, fontWeight: 600 },
      axisLine: { show: false },
      axisTick: { show: false }
    },
    series: [
      {
        type: 'bar',
        data: sorted.map((d) => d.value),
        barWidth: 14,
        showBackground: true,
        backgroundStyle: { color: 'rgba(15,31,26,0.035)', borderRadius: 99 },
        itemStyle: {
          borderRadius: 99,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#16b876' },
            { offset: 1, color: '#57e0a8' }
          ])
        },
        label: { show: true, position: 'right', color: '#0e9b62', fontWeight: 700, fontSize: 11.5 }
      }
    ]
  })
}

function renderTrend(data) {
  const chart = echarts.init(trendChartRef.value)
  charts.push(chart)
  chart.setOption({
    tooltip: baseTooltip('axis'),
    grid: { left: 10, right: 20, top: 20, bottom: 10, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map((d) => d.name),
      boundaryGap: false,
      axisLabel: { color: AXIS_COLOR, fontSize: 11 },
      axisLine: { lineStyle: { color: GRID_LINE } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: GRID_LINE } },
      axisLabel: { color: AXIS_COLOR, fontSize: 11 }
    },
    series: [
      {
        type: 'line',
        data: data.map((d) => d.value),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: false,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#16b876' },
            { offset: 1, color: '#3ddc97' }
          ])
        },
        itemStyle: { color: '#16b876', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22,184,118,0.28)' },
            { offset: 1, color: 'rgba(22,184,118,0.01)' }
          ])
        }
      }
    ]
  })
}
</script>

<style scoped>
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

@media (max-width: 900px) {
  .kpi-row {
    grid-template-columns: 1fr;
  }
}

.kpi-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  border-radius: var(--camp-radius);
  background: #fff;
  border: 1px solid var(--camp-line);
  box-shadow: var(--camp-shadow-sm);
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.kpi-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--camp-shadow-md);
}

.kpi-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 24px;
  flex-shrink: 0;
}

.kpi-icon.green {
  background: linear-gradient(135deg, #e6faf0, #d1f5e4);
}

.kpi-icon.blue {
  background: linear-gradient(135deg, #eaf1ff, #dbe7ff);
}

.kpi-icon.orange {
  background: linear-gradient(135deg, #fff3e6, #ffe6cc);
}

.kpi-body {
  position: relative;
  z-index: 1;
}

.kpi-value {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.1;
  color: var(--camp-ink);
}

.kpi-label {
  font-size: 12.5px;
  color: var(--camp-muted);
  margin-top: 4px;
  font-weight: 600;
}

.kpi-glow {
  position: absolute;
  right: -40px;
  bottom: -60px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(22, 184, 118, 0.14), transparent 65%);
  pointer-events: none;
}

.kpi-glow.blue {
  background: radial-gradient(circle, rgba(75, 123, 236, 0.12), transparent 65%);
}

.kpi-glow.orange {
  background: radial-gradient(circle, rgba(255, 138, 61, 0.14), transparent 65%);
}

/* 图表 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1000px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  padding: 22px 22px 14px;
}

.chart-card:hover {
  transform: none;
}

.chart-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 6px;
}

.chart-title {
  font-size: 15.5px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--camp-ink);
}

.chart-sub {
  font-size: 11.5px;
  color: var(--camp-muted);
  margin-top: 3px;
}

.chart {
  height: 320px;
}

.chart.trend {
  height: 290px;
}
</style>
