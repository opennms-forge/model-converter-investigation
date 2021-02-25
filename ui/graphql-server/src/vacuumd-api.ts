import { RESTDataSource } from "apollo-datasource-rest";

export type Automation = {
  actionName: string;
  triggerName: string;
  name: string;
  actionEvent: string;
  active: boolean;
  autoEventName: string
  interval: number;
};

/** Wraps the rest-api-server's API as data source to be used in resolvers */
export default class VacuumdAPI extends RESTDataSource {
  constructor() {
    super();
    this.baseURL = "http://localhost:8080/"; // this sets the base-url for the API
  }

  async getAutomations() {
    return this.get<Automation[]>(`configuration/services/vacuumd/automations/automation`);
  }

  async deleteAutomation(index) {
    this.delete<Automation>(`configuration/services/vacuumd/automations/automation/`+index);
    return true;
  }
}
