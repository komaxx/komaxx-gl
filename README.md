# komaxx-gl
Pure-Java scene-graph based multi-purpose render engine (2d, 3d, apps) for Android

Built from scratch as basis for various game and app projects (zuppl, VsFling, Stackz, ...)

## Why use it?

1. Raw performance
2. Control
3. Learn how stuff works

## Usage

### Get it to run 

No dependencies apart from AppCompat-v4.
So:
- Include the source
- Create a layout that includes a View that inherits from `BasicSceneGraphRenderView`
- Build up and manipulate your SceneGraph to your hearts content
- ..
- Profit!

### Some hints to get you started building things with komaxx-gl

1. Get some basics of OpenGl (coordinate systems, matrices, shaders)

2. Read up about SceneGraphs  

3. Start reading documentation (and code) in `Node`, `SceneGraph`, and `SceneGraphContext`/`RenderContext`. 

Check out VsFling or Stackz, two projects that are built on top of komaxx-gl.

To get *something* on screen, you'll need at least: 
* A **CameraNode** (orthographic projection will do nicely for a start). See `OrthoCameraNode`.
* A RenderProgram (a pair of Vertex and Fragment shader programs). You'll be fine with the default set `DefaultRenderProgramStore` and the `DeppenShader` to just see some colors.
* A translation Node to move things into the viewpoirt of the camera. See `TranslationNode` to go a bit into the 
* A mesh to be rendered. Let's get started with a `UnitGridNode`. Later on you'll need some meshes (or maybe just simple quads) in a node. See the BoundMesh tree for stuff like that.

From then, go to texturing (-> `Texture` / `TextureStore` / ..), and interaction (-> e.g. `ButtonInteractionInterpreter`)

And from there... just ping me and we'll figure it out together ;)

## License

MIT. Do whatever.
