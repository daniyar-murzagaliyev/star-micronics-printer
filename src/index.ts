import { registerPlugin } from '@capacitor/core';

import type { StarMicronicsPrinterPlugin } from './definitions';

const StarMicronicsPrinter = registerPlugin<StarMicronicsPrinterPlugin>(
  'StarMicronicsPrinter',
  {
    scanDevices: () => import('./web').then(m => new m.StarMicronicsPrinterWeb()),
    connectPrinter: () => import('./web').then(m => new m.StarMicronicsPrinterWeb()),
  },
);

export * from './definitions';
export { StarMicronicsPrinter };
