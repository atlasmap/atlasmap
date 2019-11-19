import { useEffect, useState } from 'react';
import { ConfigModel } from './models/config.model';
import { InitializationService } from './services/initialization.service';

export interface IAtlasmapProvider {

}

export function useAtlasmap (_: IAtlasmapProvider) {
  const [config, setConfig] = useState<ConfigModel | undefined>();
  const initializationService = new InitializationService();

  useEffect(() => {
    const initialize = async () => {
      await initializationService.initialize();
      setConfig(initializationService.cfg);
    };

    initialize();
  });

  return [config];
}