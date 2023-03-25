import { Formio, Providers } from 'formiojs';
import { minio } from './storage/minio';

export function RegisterStorageModule() {
    Providers.addProvider('storage', 'minio', minio);
    Formio.use({
        providers: Providers
    });
}
