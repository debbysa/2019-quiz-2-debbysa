package id.ac.polinema.todoretrofit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

import id.ac.polinema.todoretrofit.Application;
import id.ac.polinema.todoretrofit.Constant;
import id.ac.polinema.todoretrofit.R;
import id.ac.polinema.todoretrofit.Session;
import id.ac.polinema.todoretrofit.adapters.TodoAdapter;
import id.ac.polinema.todoretrofit.generator.ServiceGenerator;
import id.ac.polinema.todoretrofit.models.Envelope;
import id.ac.polinema.todoretrofit.models.Todo;
import id.ac.polinema.todoretrofit.models.User;
import id.ac.polinema.todoretrofit.services.AuthService;
import id.ac.polinema.todoretrofit.services.TodoService;
import id.ac.polinema.todoretrofit.services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements TodoAdapter.AdapterCallback, SearchView.OnQueryTextListener {

    private RecyclerView todosRecyclerView;
    private Session session;
    private TodoService service;
    private TodoAdapter adapter;

    public EditText todoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SaveTodoActivity.class);
                intent.putExtra(Constant.KEY_REQUEST_CODE, Constant.ADD_TODO);
                startActivityForResult(intent, Constant.ADD_TODO);
            }
        });
        session = Application.provideSession();
        if (!session.isLogin()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        todosRecyclerView = findViewById(R.id.rv_todos);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        todosRecyclerView.setLayoutManager(layoutManager);
        adapter = new TodoAdapter(this);
        todosRecyclerView.setAdapter(adapter);
        service = ServiceGenerator.createService(TodoService.class);
        loadTodos();
    }

    private void loadTodos() {
        Call<Envelope<List<Todo>>> todos = service.getTodos(null, 1, 10);
        todos.enqueue(new Callback<Envelope<List<Todo>>>() {
            @Override
            public void onResponse(Call<Envelope<List<Todo>>> call, Response<Envelope<List<Todo>>> response) {
                if (response.code() == 200) {
                    Envelope<List<Todo>> okResponse = response.body();
                    List<Todo> items = okResponse.getData();
                    adapter.setItems(items);
                }
            }

            @Override
            public void onFailure(Call<Envelope<List<Todo>>> call, Throwable t) {

            }
        });
    }

    public void onDeleteMenuClick(Todo todo) {

        int id = todo.getId();
        Call<Envelope<Todo>> deleteTodo = service.deleteTodo(Integer.toString(id));
        deleteTodo.enqueue(new Callback<Envelope<Todo>>() {
            @Override
            public void onResponse(Call<Envelope<Todo>> call, Response<Envelope<Todo>> response) {
                if (response.code() == 200) {
                    loadTodos();
                    Toast.makeText(MainActivity.this, "Delete Berhasil!",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Delete Gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Envelope<Todo>> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            session.removeSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onUpdateMenuClick(Todo todo) {
        Intent intent = new Intent(this, SaveTodoActivity.class);
        intent.putExtra(Constant.KEY_TODO, todo);
        intent.putExtra(Constant.KEY_REQUEST_CODE, Constant.UPDATE_TODO);
        startActivityForResult(intent, Constant.UPDATE_TODO);
    }

    @Override
    public void onClickItem(Todo todo) {
        int id = todo.getId();
        todo.setUser(null);
        todo.setId(null);
        todo.setUser(null);

        if(todo.getDone() == false){
            todo.getDone();
            todo.setDone(true);
            Call<Envelope<Todo>> updateTodo = service.addDone(Integer.toString(id), todo);

            updateTodo.enqueue(new Callback<Envelope<Todo>>() {
                @Override
                public void onResponse(Call<Envelope<Todo>> call, Response<Envelope<Todo>> response) {
                    if (response.code() == 200) {
                        Toast.makeText(MainActivity.this, "Tugas Selesai!", Toast.LENGTH_SHORT).show();
                        loadTodos();
                    }
                }

                @Override
                public void onFailure(Call<Envelope<Todo>> call, Throwable t) {

                }
            });
        } else if (todo.getDone() == true){
            todo.setDone(false);

            Call<Envelope<Todo>> updateTodo = service.unDone(Integer.toString(id), todo);

            updateTodo.enqueue(new Callback<Envelope<Todo>>() {
                @Override
                public void onResponse(Call<Envelope<Todo>> call, Response<Envelope<Todo>> response) {
                    if (response.code() == 200) {
                        Toast.makeText(MainActivity.this, "Tugas Belum selesai!", Toast.LENGTH_SHORT).show();
                        loadTodos();
                    }
                }

                @Override
                public void onFailure(Call<Envelope<Todo>> call, Throwable t) {

                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadTodos();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
