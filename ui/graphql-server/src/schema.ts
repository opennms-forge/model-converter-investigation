import { gql } from "apollo-server";

// A schema is a collection of type definitions (hence "typeDefs")
// that together define the "shape" of queries/mutations that are executed against
// your data.
export const typeDefs = gql`
  "Single automation instance"
  type Automation {
    name: String!
    actionName: String
    interval: Int
    triggerName: String
    autoEventName: String
    actionEvent: String
    active: Boolean
  }

  # read operations and their return types
  type Query {
    "GETs all the automations"
    automations: [Automation!]
  }

  # The mutations for updating
  type Mutation {
    "Deleted a specific automation"
    deleteAutomation(index: Int!): Boolean
  }
`;
