import { applyOffset } from '../utils/chartHelpers';

// БАЗОВЫЕ КООРДИНАТЫ
export const BASE_COORDS = {
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
export const POINT_OFFSETS = {
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

export const GREEN_LINE_POINTS = {};
export const PURPLE_LINE_POINTS = {};

Object.keys(BASE_COORDS.green).forEach(date => {
    GREEN_LINE_POINTS[date] = applyOffset(date, BASE_COORDS.green, POINT_OFFSETS.roi);
});

Object.keys(BASE_COORDS.purple).forEach(date => {
    PURPLE_LINE_POINTS[date] = applyOffset(date, BASE_COORDS.purple, POINT_OFFSETS.purple);
});

// ==================== КОНЕЦ НАСТРОЕК ====================
