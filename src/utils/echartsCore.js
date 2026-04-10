/**
 * ECharts 按需注册（bar / line / pie + 本项目用到的组件），避免全量 echarts 进首包。
 */
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  AxisPointerComponent,
  GridComponent,
  LegendComponent,
  MarkLineComponent,
  TooltipComponent,
  ToolboxComponent
} from 'echarts/components'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  ToolboxComponent,
  MarkLineComponent,
  AxisPointerComponent,
  UniversalTransition
])

/** 折线图区域渐变（等价于原 echarts.graphic.LinearGradient，便于 tree-shaking） */
export function lineAreaGradient(stops) {
  return {
    type: 'linear',
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: stops
  }
}

export default echarts
