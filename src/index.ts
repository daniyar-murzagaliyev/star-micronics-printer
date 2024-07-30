import { registerPlugin } from '@capacitor/core';

import type { StarMicronicsPrinterPlugin } from './definitions';

const StarMicronicsPrinter = registerPlugin<StarMicronicsPrinterPlugin>(
  'StarMicronicsPrinter',
  {
    web: () => import('./web').then(m => new m.StarMicronicsPrinterWeb()),
  },
);

export * from './definitions';
export { StarMicronicsPrinter };
