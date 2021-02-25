import { Resolvers } from "./generated/graphql";
import { DataSources } from "./data-sources";

/** Gets used in Resolver generation via `graphql-server/codegen.yml`  */
export type Context = { dataSources: DataSources };

/**
 * Simple query and mutation resolvers, plus additional resolver
 * for the Automation to handle naming source fields with dashes.
 */
export const resolvers: Resolvers = {
  Query: {
    automations: async (parent, args, { dataSources }) =>
      dataSources.vacuumdAPI.getAutomations(),
  },
  Mutation: {
    deleteAutomation: async (_, { index }, { dataSources }) => 
      dataSources.vacuumdAPI.deleteAutomation(index),
  },
  Automation: {
    actionName(parent) { return parent['action-name']; },
    triggerName(parent) { return parent['trigger-name']; },
    autoEventName(parent) { return parent['auto-event-name']; },
    actionEvent(parent) { return parent['action-event']; },
  },
};

