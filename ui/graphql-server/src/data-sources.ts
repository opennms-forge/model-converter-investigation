import VacuumdAPI from "./vacuumd-api";

/**
 * Simple vacuumd based data source
 */
export const dataSources = () => ({
  vacuumdAPI: new VacuumdAPI(), // just one for now
});

export type DataSources = ReturnType<typeof dataSources>;
