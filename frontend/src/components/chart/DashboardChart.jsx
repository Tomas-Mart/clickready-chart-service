import React, {useEffect, useRef, useState} from 'react';
import {ChartStateContext} from './context/ChartStateContext';
import {Area, Bar, ComposedChart, Line, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';

import {COLORS} from './config/colors';
import {SCALES} from './config/scales';
import {BASE_POINTS, generateChartData} from './config/data';
import {GREEN_LINE_POINTS, PURPLE_LINE_POINTS} from './config/coordinates';
import {CustomTooltip} from './CustomTooltip';
import {GreenHaloDot, PurpleHaloDot, YellowHaloDot} from './HaloDots';
import {GreenDiamondDot, PurpleSquareDot, YellowCircleDot} from './CustomDots';

// ==================== ОСНОВНОЙ КОМПОНЕНТ ====================

const DashboardChart = () => {
    const chartData = generateChartData();
    const [activeDate, setActiveDate] = useState(null);
    const [selectedDate, setSelectedDate] = useState(null);
    const [hoveredPurple, setHoveredPurple] = useState(null);
    const chartRef = useRef(null);

    console.log('🔍 ChartStateContext в DashboardChart:', ChartStateContext);
    console.log('🔍 activeDate в DashboardChart:', activeDate);

    // ✅ Создаем объект со всеми состояниями для контекста
    const chartState = {
        activeDate,
        setActiveDate,
        selectedDate,
        setSelectedDate,
        hoveredPurple,
        setHoveredPurple,
    };

    // Дополнительный лог для отслеживания изменений activeDate
    useEffect(() => {
        console.log('🔄 activeDate ИЗМЕНИЛСЯ в DashboardChart:', activeDate);
    }, [activeDate]);

    const mainData = BASE_POINTS.map((point, index) => ({
        date: point.date,
        rawDate: point.date,
        cost: point.cost,
        costScaled: point.cost * SCALES.cost,
        cpa: point.cpa,
        cpaScaled: point.cpa * SCALES.cpa,
        roi: point.roi,
        rooScaled: point.roi * SCALES.roi,
        conversions: point.conversions,
        conversionsScaled: point.conversions * SCALES.conversions,
        isMidPoint: false,
        x: index,
    }));

    const cpaData = mainData.map(item => ({
        date: item.date,
        cpaScaled: item.cpaScaled,
        isMidPoint: false,
        x: item.x,
    }));

    const roiFullData = chartData.map(item => ({
        date: item.date,
        rawDate: item.rawDate,
        roi: item.roi,
        rooScaled: item.roi * SCALES.roi,
        isMidPoint: item.isMidPoint,
        x: item.x,
    }));

    const conversionsData = mainData.map(item => ({
        date: item.date,
        rawDate: item.date,
        conversions: item.conversions,
        conversionsScaled: item.conversions * SCALES.conversions,
        isMidPoint: false,
        x: item.x,
    }));

    useEffect(() => {
        // Отладочная информация о состоянии
        console.log('📊 DashboardChart state:', {activeDate, selectedDate, hoveredPurple});

        // Размеры графика
        if (chartRef.current) {
            console.log('📐 Chart dimensions:', chartRef.current.getBoundingClientRect());
        }

        // Вывод всех точек с координатами
        console.log('📊 Данные точек:');
        mainData.forEach((point, index) => {
            console.log(`  Точка ${index + 1} (${point.date}):`, {
                cost: point.cost,
                costScaled: point.costScaled,
                roi: point.roi,
                rooScaled: point.roi * SCALES.roi,
                conversions: point.conversions,
                conversionsScaled: point.conversions * SCALES.conversions,
                cpa: point.cpa,
                cpaScaled: point.cpa * SCALES.cpa,
                x: point.x
            });
        });

        // Координаты зеленой линии
        console.log('🟢 Точные координаты зеленой линии (ROI):');
        Object.entries(GREEN_LINE_POINTS).forEach(([date, coords]) => {
            console.log(`  ${date}: cx=${coords.cx}, cy=${coords.cy}`);
        });

        // Координаты фиолетовой линии
        console.log('🟣 Точные координаты фиолетовой линии (Conversions):');
        Object.entries(PURPLE_LINE_POINTS).forEach(([date, coords]) => {
            console.log(`  ${date}: cx=${coords.cx}, cy=${coords.cy}`);
        });

        // Порядок линий в конце графика
        const lastPoint = mainData[mainData.length - 1];
        console.log('📊 Порядок линий в конце графика (14.06.2026):');
        console.log(`  🟡 Cost: ${lastPoint.costScaled}`);
        console.log(`  🟣 Conversions: ${lastPoint.conversionsScaled}`);
        console.log(`  🟢 ROI: ${lastPoint.roi * SCALES.roi}`);
        console.log(`  🔵 CPA: ${lastPoint.cpa * SCALES.cpa}`);

        // Правильный порядок (сверху вниз)
        const values = [
            {name: 'Cost', value: lastPoint.costScaled, color: '🟡'},
            {name: 'Conversions', value: lastPoint.conversionsScaled, color: '🟣'},
            {name: 'ROI', value: lastPoint.roi * SCALES.roi, color: '🟢'},
            {name: 'CPA', value: lastPoint.cpa * SCALES.cpa, color: '🔵'}
        ];
        values.sort((a, b) => b.value - a.value);
        console.log('📊 Правильный порядок (сверху вниз):');
        values.forEach((item, index) => {
            console.log(`  ${index + 1}. ${item.color} ${item.name}: ${item.value}`);
        });
    }, [activeDate, selectedDate, hoveredPurple, mainData]);

    // Функция-обертка для отладки
    const handleSetActiveDate = (date) => {
        console.log('📌 Устанавливаем activeDate в DashboardChart:', date);
        setActiveDate(date);
    };

    return (
        <ChartStateContext.Provider value={chartState}>
            <div
                ref={chartRef}
                className="w-full max-w-4xl mx-auto h-[420px] bg-[#FDE8E8] p-4 rounded-lg border border-gray-300 flex relative"
                onClick={() => {
                    console.log('📊 Click on background - clearing selectedDate');
                    setSelectedDate(null);
                    setHoveredPurple(null);
                }}
            >
                <div className="w-16 h-full flex flex-col justify-between py-10 pr-3 z-10">
                    <div
                        className="bg-white rounded px-2 py-1 text-center text-gray-500 font-medium text-[12px] shadow-sm">Tdy
                    </div>
                    <div
                        className="bg-white rounded px-2 py-1 text-center text-gray-500 font-medium text-[12px] shadow-sm">0%
                    </div>
                    <div className="text-center text-gray-500 font-medium text-[12px]">$0</div>
                    <div
                        className="bg-white rounded px-2 py-1 text-center text-gray-500 font-medium text-[12px] shadow-sm">0
                    </div>
                    <div className="text-center text-gray-500 font-medium text-[12px]">0</div>
                </div>

                <div className="flex-1 h-full pt-2 pb-4 pr-4 relative border border-gray-400 bg-transparent">
                    <ResponsiveContainer width="100%" height="100%">
                        <ComposedChart
                            data={mainData}
                            margin={{top: 10, right: 30, left: 10, bottom: 10}}
                        >
                            <XAxis
                                dataKey="x"
                                tick={false}
                                axisLine={false}
                                tickLine={false}
                                type="number"
                                domain={[0, 4]}
                                padding={{left: 10, right: 10}}
                            />
                            <YAxis
                                yAxisId="left"
                                orientation="left"
                                tick={{fontSize: 0}}
                                axisLine={false}
                                tickLine={false}
                                domain={[0, 700]}
                            />
                            <YAxis
                                yAxisId="right"
                                orientation="right"
                                tick={{fontSize: 0}}
                                axisLine={false}
                                tickLine={false}
                                domain={[0, 100]}
                            />

                            <Tooltip content={<CustomTooltip/>} cursor={false} isAnimationActive={false}/>

                            <Area
                                yAxisId="left"
                                type="basis"
                                dataKey="costScaled"
                                data={chartData}
                                stroke="none"
                                fill={COLORS.yellowArea}
                                fillOpacity={1}
                            />

                            <Bar
                                yAxisId="left"
                                dataKey="cpaScaled"
                                data={cpaData}
                                fill={COLORS.blue}
                                barSize={30}
                                isAnimationActive={false}
                            />

                            {/* Желтая линия Cost */}
                            <Line
                                yAxisId="left"
                                type="basis"
                                dataKey="costScaled"
                                data={chartData}
                                stroke={COLORS.yellowFade}
                                strokeWidth={3}
                                dot={null}
                                activeDot={(props) => {
                                    const {key, ...rest} = props;
                                    if (rest.payload?.isMidPoint) return null;
                                    return (
                                        <React.Fragment key={key}>
                                            <YellowHaloDot {...rest} />
                                            <YellowCircleDot {...rest} />
                                        </React.Fragment>
                                    );
                                }}
                                onMouseEnter={(e) => {
                                    if (e && e.payload && e.payload.rawDate && !e.payload.isMidPoint) {
                                        handleSetActiveDate(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    handleSetActiveDate(null);
                                }}
                            />

                            {/* Зеленая линия ROI */}
                            <Line
                                yAxisId="left"
                                type="basis"
                                dataKey="rooScaled"
                                data={roiFullData}
                                stroke={COLORS.green}
                                strokeWidth={4}
                                dot={null}
                                activeDot={(props) => {
                                    const {key, ...rest} = props;
                                    if (rest.payload?.isMidPoint) return null;
                                    return (
                                        <React.Fragment key={key}>
                                            <GreenHaloDot {...rest} />
                                            <GreenDiamondDot {...rest} />
                                        </React.Fragment>
                                    );
                                }}
                                onMouseEnter={(e) => {
                                    if (e && e.payload && e.payload.rawDate && !e.payload.isMidPoint) {
                                        handleSetActiveDate(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    handleSetActiveDate(null);
                                }}
                            />

                            {/* Фиолетовая линия Conversions - с отладкой и правильным порядком */}
                            <Line
                                yAxisId="right"
                                type="linear"
                                dataKey="conversionsScaled"
                                data={conversionsData}
                                stroke={COLORS.purple}
                                strokeWidth={2.5}
                                dot={(props) => {
                                    const {key, ...rest} = props;
                                    if (rest.payload?.isMidPoint) return null;
                                    const rawDate = rest.payload?.rawDate;
                                    const isHovered = hoveredPurple === rawDate;
                                    return (
                                        <g key={key}>
                                            {/* Бледный круг - показываем всегда, если hoveredPurple === rawDate */}
                                            {isHovered && <PurpleHaloDot {...rest} activeDate={hoveredPurple}/>}
                                            {/* Квадратик - всегда виден, но меняет размер при наведении */}
                                            <PurpleSquareDot {...rest} active={isHovered}/>
                                        </g>
                                    );
                                }}
                                activeDot={null}  // ← ОТКЛЮЧАЕМ activeDot
                                onMouseEnter={(e) => {
                                    if (e && e.payload && e.payload.rawDate) {
                                        console.log(`🟣 Purple line onMouseEnter: ${e.payload.rawDate}`);
                                        handleSetActiveDate(e.payload.rawDate);
                                        setHoveredPurple(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    console.log('🟣 Purple line onMouseLeave');
                                    handleSetActiveDate(null);
                                    setHoveredPurple(null);
                                }}
                            />

                            <ReferenceLine y={0} stroke="#ccc" strokeDasharray="3 3" yAxisId="left"/>
                        </ComposedChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </ChartStateContext.Provider>
    );
};

export default DashboardChart;