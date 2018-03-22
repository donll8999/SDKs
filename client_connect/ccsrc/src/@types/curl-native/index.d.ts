declare module "curl-native" {
  interface Request {
    url?: string;
    userAgent?: string;
    method?: string;
    headers?: string[];
  }

  interface Response {
    responseText: string;
    statusCode: number;
  }

  function fetch(req: Request, resolve: (res: Response) => void, reject: (err: Error) => void): void;
}