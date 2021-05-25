/*
    Copyright (C) 2017 Red Hat, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
import { INotification } from '../../Views';

export interface ResetNotificationsAction {
  type: 'reset';
}

export interface UpdateNotificationsAction {
  type: 'update';
  payload: {
    notifications: INotification[];
  };
}

export interface DismissNotificationAction {
  type: 'dismiss';
  payload: {
    id: string;
  };
}

export interface INotificationsState {
  notifications: INotification[];
}

export function initNotificationsState(): INotificationsState {
  return {
    notifications: [],
  };
}

export function notificationsReducer(
  state: INotificationsState,
  action:
    | ResetNotificationsAction
    | UpdateNotificationsAction
    | DismissNotificationAction,
): INotificationsState {
  switch (action.type) {
    case 'reset':
      return {
        notifications: [],
      };
    case 'update':
      return {
        notifications: action.payload.notifications.map((n) => ({
          ...n,
          isRead: state.notifications.find((on) => on.id === n.id)?.isRead,
        })),
      };
    case 'dismiss':
      return {
        notifications: state.notifications.map((n) =>
          n.id === action.payload.id ? { ...n, isRead: true } : n,
        ),
      };
    default:
      return state;
  }
}
