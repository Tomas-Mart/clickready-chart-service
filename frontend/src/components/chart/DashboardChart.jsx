import React, {createContext, useContext, useEffect, useRef, useState} from 'react';
import {Area, Bar, ComposedChart, Line, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis,} from 'recharts';

const COLORS = {
    yellow: '#FBBF24',
    yellowFade: '#FDE047',
    yellowArea: 'rgba(253, 224, 71, 0.35)',
    purple: '#9333EA',
    purpleFade: 'rgba(147, 51, 234, 0.25)',
    green: '#15803D',
    greenFade: 'rgba(21, 128, 61, 0.25)',
    blue: '#2563EB',
};

// ==================== НАСТРОЙКИ ДЛЯ РУЧНОЙ КОРРЕКТИРОВКИ ====================
const SCALES = {
    cost: 10,
    conversions: 1,
    roi: 1,
    cpa: 10,
};

// БАЗОВЫЕ КООРДИНАТЫ
const BASE_COORDS = {
    green: {
        '10.06.2026': {cx: 80, cy: 49.64},
        '11.06.2026': {cx: 230, cy: 227.7},
        '12.06.2026': {cx: 380, cy: 249.26},
        '13.06.2026': {cx: 530, cy: 285},
        '14.06.2026': {cx: 680, cy: 162.28},
    },
    purple: {
        '10.06.2026': {cx: 80, cy: 310.7},
        '11.06.2026': {cx: 230, cy: 227},
        '12.06.2026': {cx: 380, cy: 208.4},
        '13.06.2026': {cx: 530, cy: 103},
        '14.06.2026': {cx: 680, cy: 41},
    },
};

// КОРРЕКТИРОВКА ОФФСЕТОВ (ручная настройка)
const POINT_OFFSETS = {
    roi: {
        '10.06.2026': {cx: 0, cy: 0},
        '11.06.2026': {cx: 0, cy: 0},
        '12.06.2026': {cx: 0, cy: 0},
        '13.06.2026': {cx: 0, cy: 0},
        '14.06.2026': {cx: 0, cy: 0},
    },
    purple: {
        '10.06.2026': {cx: 0, cy: 0},
        '11.06.2026': {cx: 0, cy: 0},
        '12.06.2026': {cx: 0, cy: 0},
        '13.06.2026': {cx: 0, cy: 0},
        '14.06.2026': {cx: 0, cy: 0},
    },
    yellow: {
        '10.06.2026': {cx: 0, cy: 0},
        '11.06.2026': {cx: 0, cy: 0},
        '12.06.2026': {cx: 0, cy: 0},
        '13.06.2026': {cx: 0, cy: 0},
        '14.06.2026': {cx: 0, cy: 0},
    },
};

// Применяем оффсеты
const applyOffset = (date, baseCoords, offsets) => {
    const base = baseCoords[date];
    const offset = offsets[date] || {cx: 0, cy: 0};
    return {
        cx: base.cx + offset.cx,
        cy: base.cy + offset.cy,
    };
};

const GREEN_LINE_POINTS = {};
const PURPLE_LINE_POINTS = {};

Object.keys(BASE_COORDS.green).forEach(date => {
    GREEN_LINE_POINTS[date] = applyOffset(date, BASE_COORDS.green, POINT_OFFSETS.roi);
});

Object.keys(BASE_COORDS.purple).forEach(date => {
    PURPLE_LINE_POINTS[date] = applyOffset(date, BASE_COORDS.purple, POINT_OFFSETS.purple);
});

// ==================== КОНЕЦ НАСТРОЕК ====================

const BASE_POINTS = [
    {date: '10.06.2026', cost: 2.04, cpa: 0.68, roi: 610.78, conversions: 3},
    {date: '11.06.2026', cost: 25.85, cpa: 0.86, roi: 180.5, conversions: 30},
    {date: '12.06.2026', cost: 44.36, cpa: 1.23, roi: 161.47, conversions: 36},
    {date: '13.06.2026', cost: 55.65, cpa: 0.79, roi: 56.33, conversions: 70},
    {date: '14.06.2026', cost: 63.75, cpa: 0.71, roi: 357.25, conversions: 90},
];

const generateChartData = () => {
    const result = [];

    BASE_POINTS.forEach((point, index) => {
        result.push({
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
        });

        if (index < BASE_POINTS.length - 1) {
            const next = BASE_POINTS[index + 1];
            const t1 = 1 / 3;
            const midCost1 = point.cost + (next.cost - point.cost) * t1;
            const midCpa1 = point.cpa + (next.cpa - point.cpa) * t1;
            const midRoi1 = point.roi + (next.roi - point.roi) * t1;
            const midConversions1 = point.conversions + (next.conversions - point.conversions) * t1;

            result.push({
                date: `${point.date}_mid1`,
                rawDate: point.date,
                cost: midCost1,
                costScaled: midCost1 * SCALES.cost,
                cpa: midCpa1,
                cpaScaled: midCpa1 * SCALES.cpa,
                roi: midRoi1,
                rooScaled: midRoi1 * SCALES.roi,
                conversions: midConversions1,
                conversionsScaled: midConversions1 * SCALES.conversions,
                isMidPoint: true,
                x: index + t1,
            });

            const t2 = 2 / 3;
            const midCost2 = point.cost + (next.cost - point.cost) * t2;
            const midCpa2 = point.cpa + (next.cpa - point.cpa) * t2;
            const midRoi2 = point.roi + (next.roi - point.roi) * t2;
            const midConversions2 = point.conversions + (next.conversions - point.conversions) * t2;

            result.push({
                date: `${point.date}_mid2`,
                rawDate: point.date,
                cost: midCost2,
                costScaled: midCost2 * SCALES.cost,
                cpa: midCpa2,
                cpaScaled: midCpa2 * SCALES.cpa,
                roi: midRoi2,
                rooScaled: midRoi2 * SCALES.roi,
                conversions: midConversions2,
                conversionsScaled: midConversions2 * SCALES.conversions,
                isMidPoint: true,
                x: index + t2,
            });
        }
    });

    return result;
};

const ActiveDateContext = createContext(null);

const CustomTooltip = ({active, payload}) => {
    if (active && payload && payload.length) {
        const pointData = payload[0]?.payload;
        if (!pointData || pointData.isMidPoint) return null;

        const {rawDate, cost, cpa, roi, conversions} = pointData;

        return (
            <div
                style={{
                    background: 'white',
                    padding: '12px 16px',
                    borderRadius: 8,
                    boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                    border: '1px solid #e5e7eb',
                    minWidth: 190,
                    fontFamily: 'system-ui, sans-serif',
                }}
            >
                <div style={{fontWeight: 600, fontSize: 14, color: '#111827', marginBottom: 8}}>
                    {rawDate || ''}
                </div>
                <div style={{display: 'flex', flexDirection: 'column', gap: 4}}>
                    {cost !== undefined && cost !== null && (
                        <MetricRow color={COLORS.yellowFade} label="Cost" value={cost.toFixed(2)}/>
                    )}
                    {cpa !== undefined && cpa !== null && (
                        <MetricRow color={COLORS.blue} label="CPA" value={cpa.toFixed(2)}/>
                    )}
                    {roi !== undefined && roi !== null && (
                        <MetricRow color={COLORS.green} label="ROI confirmed" value={roi.toFixed(2)}/>
                    )}
                    {conversions !== undefined && conversions !== null && (
                        <MetricRow color={COLORS.purple} label="Conversions" value={conversions}/>
                    )}
                </div>
            </div>
        );
    }
    return null;
};

const MetricRow = ({color, label, value}) => (
    <div style={{display: 'flex', alignItems: 'center', fontSize: 13, color: '#374151'}}>
        <span
            style={{
                display: 'inline-block',
                width: 12,
                height: 12,
                borderRadius: '50%',
                background: color,
                marginRight: 10,
            }}
        />
        <span style={{marginRight: 6}}>{label}:</span>
        <b style={{fontWeight: 700, color: '#111827'}}>{value}</b>
    </div>
);

// ==================== HALO-ЭФФЕКТЫ С ОТЛАДКОЙ ====================

const YellowHaloDot = (props) => {
    const {cx, cy, payload} = props;
    const activeDate = useContext(ActiveDateContext);

    const isActive = payload?.rawDate === activeDate;
    console.log(`🟡 YellowHaloDot: ${payload?.rawDate}, activeDate=${activeDate}, isActive=${isActive}`);

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== activeDate) return null;

    console.log(`✅🟡 YellowHaloDot ОТОБРАЖЕН для ${payload.rawDate}`);

    return (
        <circle
            cx={cx}
            cy={cy}
            r={14}
            fill={COLORS.yellowFade}
            opacity={0.6}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};

const GreenHaloDot = (props) => {
    const {cx, cy, payload} = props;
    const activeDate = useContext(ActiveDateContext);

    const isActive = payload?.rawDate === activeDate;
    console.log(`🟢 GreenHaloDot: ${payload?.rawDate}, activeDate=${activeDate}, isActive=${isActive}`);

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== activeDate) return null;

    const exactPoint = GREEN_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    console.log(`✅🟢 GreenHaloDot ОТОБРАЖЕН для ${payload.rawDate} на координатах cx=${exactCx}, cy=${exactCy}`);

    return (
        <circle
            cx={exactCx}
            cy={exactCy}
            r={14}
            fill={COLORS.greenFade}
            opacity={0.7}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};

// Хало-эффект для фиолетовой линии (Conversions) - с поддержкой activeDate из пропсов
const PurpleHaloDot = (props) => {
    const {cx, cy, payload, activeDate: propActiveDate} = props;
    const contextActiveDate = useContext(ActiveDateContext);

    // Используем activeDate из пропсов, если передано, иначе из контекста
    const currentActiveDate = propActiveDate !== undefined ? propActiveDate : contextActiveDate;

    if (cx == null || cy == null || !payload || payload.isMidPoint) return null;
    if (payload.rawDate !== currentActiveDate) return null;

    const exactPoint = PURPLE_LINE_POINTS[payload.rawDate];
    const exactCx = exactPoint ? exactPoint.cx : cx;
    const exactCy = exactPoint ? exactPoint.cy : cy;

    console.log(`✅🟣 PurpleHaloDot ОТОБРАЖЕН для ${payload.rawDate} на координатах cx=${exactCx}, cy=${exactCy}`);

    return (
        <circle
            cx={exactCx}
            cy={exactCy}
            r={14}
            fill={COLORS.purpleFade}
            opacity={0.6}
            style={{mixBlendMode: 'multiply'}}
        />
    );
};

// ==================== ОСНОВНОЙ КОМПОНЕНТ ====================

const DashboardChart = () => {
    const chartData = generateChartData();
    const [activeDate, setActiveDate] = useState(null);
    const [selectedDate, setSelectedDate] = useState(null);
    const [hoveredPurple, setHoveredPurple] = useState(null);
    const chartRef = useRef(null);

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

    // ==================== КАСТОМНЫЕ ТОЧКИ ====================

    // Зеленый ромб
    const GreenDiamondDot = ({cx, cy, payload}) => {
        if (cx == null || cy == null || !payload || payload.isMidPoint) return null;

        const exactPoint = GREEN_LINE_POINTS[payload.rawDate];
        const exactCx = exactPoint ? exactPoint.cx : cx;
        const exactCy = exactPoint ? exactPoint.cy : cy;

        const size = 3;
        const path = `M${exactCx},${exactCy - size} L${exactCx + size},${exactCy} L${exactCx},${exactCy + size} L${exactCx - size},${exactCy} Z`;

        return (
            <path
                d={path}
                fill={COLORS.green}
                stroke="white"
                strokeWidth={2}
                style={{cursor: 'pointer'}}
                onClick={() => {
                    console.log(`🟢 Клик на ромбе (ROI) ${payload.rawDate}: cx=${exactCx}, cy=${exactCy}`);
                }}
            />
        );
    };

    // Квадратик для фиолетовой линии (Conversions) с отладкой
    const PurpleSquareDot = ({cx, cy, payload, active}) => {
        if (cx == null || cy == null || !payload || payload.isMidPoint) return null;

        const exactPoint = PURPLE_LINE_POINTS[payload.rawDate];
        const exactCx = exactPoint ? exactPoint.cx : cx;
        const exactCy = exactPoint ? exactPoint.cy : cy;

        if (selectedDate === payload.rawDate) {
            return null;
        }

        // Используем проп active от Recharts или состояние hoveredPurple
        const isActive = active || hoveredPurple === payload.rawDate;

        const size = isActive ? 5 : 8;
        const strokeWidth = isActive ? 2.5 : 0;

        console.log(`🟣 Квадратик ${payload.rawDate}: isActive=${isActive}, hoveredPurple=${hoveredPurple}, activeDate=${activeDate}`);

        return (
            <g
                style={{cursor: 'pointer'}}
                onMouseEnter={() => {
                    console.log(`🟣🟣🟣 НАВЕДЕНИЕ НА КВАДРАТИК ${payload.rawDate} 🟣🟣🟣`);
                    console.log(`   - Устанавливаем hoveredPurple: ${payload.rawDate}`);
                    console.log(`   - Устанавливаем activeDate: ${payload.rawDate}`);
                    setHoveredPurple(payload.rawDate);
                    setActiveDate(payload.rawDate);
                }}
                onMouseLeave={() => {
                    console.log(`🟣🟣🟣 УХОД С КВАДРАТИКА ${payload.rawDate} 🟣🟣🟣`);
                    console.log(`   - Сбрасываем hoveredPurple и activeDate`);
                    setHoveredPurple(null);
                    setActiveDate(null);
                }}
                onClick={() => {
                    console.log(`🟣 Клик на кубике (Conversions) ${payload.rawDate}: cx=${exactCx}, cy=${exactCy}`);
                    setSelectedDate(payload.rawDate);
                }}
            >
                <rect
                    x={exactCx - size / 2}
                    y={exactCy - size / 2}
                    width={size}
                    height={size}
                    fill={COLORS.purple}
                    stroke="white"
                    strokeWidth={strokeWidth}
                    style={{
                        transition: 'all 0.15s ease-in-out',
                        transformOrigin: `${exactCx}px ${exactCy}px`,
                    }}
                />
            </g>
        );
    };

    // Желтый круг
    const YellowCircleDot = ({cx, cy, payload}) => {
        if (cx == null || cy == null || !payload || payload.isMidPoint) return null;

        const rawDate = payload.rawDate;
        const offset = POINT_OFFSETS.yellow[rawDate] || {cx: 0, cy: 0};
        const finalCx = cx + offset.cx;
        const finalCy = cy + offset.cy;

        return (
            <circle
                cx={finalCx}
                cy={finalCy}
                r={3}
                fill={COLORS.yellowFade}
                stroke="white"
                strokeWidth={2}
                style={{cursor: 'pointer'}}
                onClick={() => {
                    console.log(`🟡 Клик на желтой точке ${payload.rawDate}: cx=${finalCx}, cy=${finalCy}`);
                }}
            />
        );
    };

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

    return (
        <ActiveDateContext.Provider value={activeDate}>
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
                                        setActiveDate(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    setActiveDate(null);
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
                                        setActiveDate(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    setActiveDate(null);
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
                                        setActiveDate(e.payload.rawDate);
                                        setHoveredPurple(e.payload.rawDate);
                                    }
                                }}
                                onMouseLeave={() => {
                                    console.log('🟣 Purple line onMouseLeave');
                                    setActiveDate(null);
                                    setHoveredPurple(null);
                                }}
                            />

                            <ReferenceLine y={0} stroke="#ccc" strokeDasharray="3 3" yAxisId="left"/>
                        </ComposedChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </ActiveDateContext.Provider>
    );
};

export default DashboardChart;