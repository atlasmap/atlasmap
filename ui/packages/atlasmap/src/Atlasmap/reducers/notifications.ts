import { INotification } from "../../Views";

export interface ResetNotificationsAction {
  type: "reset";
}

export interface UpdateNotificationsAction {
  type: "update";
  payload: {
    notifications: INotification[];
  };
}

export interface DismissNotificationAction {
  type: "dismiss";
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
    case "reset":
      return {
        notifications: [],
      };
    case "update":
      return {
        notifications: action.payload.notifications.map((n) => ({
          ...n,
          isRead: state.notifications.find((on) => on.id === n.id)?.isRead,
        })),
      };
    case "dismiss":
      return {
        notifications: state.notifications.map((n) =>
          n.id === action.payload.id ? { ...n, isRead: true } : n,
        ),
      };
    default:
      return state;
  }
}
