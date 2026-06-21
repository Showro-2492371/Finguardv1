// import { RenderMode, ServerRoute } from '@angular/ssr';
//
// export const serverRoutes: ServerRoute[] = [
//   {
//     path: '**',
//     renderMode: RenderMode.Prerender
//   }
// ];


import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // 1. Render protected user and admin sections entirely in the browser (Client)
  {
    path: 'user/**',
    renderMode: RenderMode.Client
  },
  {
    path: 'admin/**',
    renderMode: RenderMode.Client
  },
  // 2. Public auth pages (login/signup) can also be rendered on the Client
  // to prevent flicker during credential checks
  {
    path: 'user/login',
    renderMode: RenderMode.Client
  },
  {
    path: 'admin/login',
    renderMode: RenderMode.Client
  },
  // 3. Fallback/Default for any other pages (like static landing/error pages)
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
