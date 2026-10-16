
- MapView dentro de Composable com `clip` (cantos arredondados) precisa de TextureView (`MapInitOptions(context, textureView = true)`). Com SurfaceView o mapa fica preto. Ao migrar libs, não remover opções do código antigo sem entender o motivo.
